<i18n>
{
  "en": {
    "cantUpload": "You can't upload when files are sending",
    "upload": "Drop your files / directories !",
    "cantUploadPermission": "You do not have the required permissions to upload studies.",
    "cancel": "Cancel"
  },
  "fr": {
    "cantUpload": "Vous ne pouvez pas charger d'autres fichiers pendant un envoi.",
    "upload": "Lâcher vos fichiers / dossiers !",
    "cantUploadPermission": "Vous n'avez pas les permissions requises pour charger des études.",
    "cancel": "Annuler"
  }
}
</i18n>

<template>
  <div
    id="file-drag-drop"
    ref="filedragdrop"
  >
    <!--
      https://stackoverflow.com/questions/34817656/add-class-in-drop-area-file-input-when-dragging-an-external-image-over-dragen
    -->
    <form
      id="fileform"
      ref="fileform"
      :class="['fileform', hover ? 'dragenterFormClass' : '']"
    >
      <div
        id="dragingcomponenet"
        ref="dragingcomponenet"
        class="dragingcomponenet"
      >
        <div
          v-if="hover"
          class="outPopUp"
        >
          <p
            v-if="!canUpload"
          >
            {{ $t("cantUploadPermission") }}
          </p>
          <p
            v-else-if="sending && files.length > 0"
          >
            <span>
              {{ $t("cantUpload") }}
            </span>
          </p>
          <p
            v-else
          >
            {{ $t("upload") }}
          </p>
        </div>
        <div
          v-if="loading"
          class="outPopUp"
        >
          <clip-loader
            :loading="loading"
            :size="'60px'"
            :color="'white'"
          />
        </div>
        <div
          :class="['dropzone-area', classDragIn]"
        >
          <slot name="dropzone-content">
            <list
              ref="list"
              :permissions="permissions"
              :album-i-d="albumID"
              @loadfiles="inputLoadFiles"
              @loaddirectories="inputLoadFiles"
            />
          </slot>
        </div>
      </div>
    </form>
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
import ClipLoader from 'vue-spinner/src/ClipLoader.vue';
import List from '@/components/studieslist/List';
import mobiledetect from '@/mixins/mobiledetect.js';

export default {
  name: 'ComponentDragAndDrop',
  components: { ClipLoader, List },
  props: {
    permissions: {
      type: Object,
      required: true,
      default: () => ({}),
    },
    albumID: {
      type: String,
      required: false,
      default: undefined,
    },
  },
  data() {
    return {
      dragAndDropCapable: false,
      hover: false,
      excludeFiles: ['DICOMDIR', '.DS_Store'],
      count: 0,
      counterDraging: 0,
      loading: false,
    };
  },
  computed: {
    ...mapGetters({
      sending: 'sending',
      files: 'files',
      demoDragAndDrop: 'demoDragAndDrop',
      source: 'source',
    }),
    canUpload() {
      return this.permissions.add_series;
    },
    classDragIn() {
      if (!mobiledetect.mobileAndTabletcheck()) {
        if (this.hover || this.loading) {
          return 'dragenterClass';
        }
        return 'dragNotEnterFormClass';
      }
      return '';
    },
  },
  watch: {
    demoDragAndDrop() {
      if (this.demoDragAndDrop) {
        this.hover = true;
        setTimeout(() => {
          this.hover = false;
          this.$store.dispatch('setDemoDragAndDrop', false);
        }, 1500);
      }
    },
  },
  created() {
  },
  mounted() {
    this.dragAndDropCapable = this.determineDragAndDropCapable();
    if (this.dragAndDropCapable) {
      // For each event add an event listener that prevents the default action
      ['drag', 'dragstart', 'dragend', 'dragover', 'dragenter', 'dragleave', 'drop'].forEach((evt) => {
        this.$refs.fileform.addEventListener(evt, (e) => {
          e.preventDefault();
          e.stopPropagation();
        }, false);
      });

      // Capture the files from the drop event and add them to local files array
      this.$refs.fileform.addEventListener('drop', async (e) => {
        if (this.hover) this.hover = false;
        if (!this.sending && !this.loading && this.canUpload) {
          this.loading = true;
          this.manageDataTransfer(e.dataTransfer.items);
        }
        this.counterDraging -= 1;
      });

      /*
      https://stackoverflow.com/questions/7110353/html5-dragleave-fired-when-hovering-a-child-element
      //$refs.dragingcomponenet
      */
      this.$refs.fileform.addEventListener('dragenter', () => {
        this.counterDraging += 1;
        this.hover = true;
      });

      this.$refs.fileform.addEventListener('dragleave', () => {
        this.counterDraging -= 1;
        if (this.counterDraging === 0) {
          this.hover = false;
        }
      });
    }
  },
  methods: {
    storeFiles(files) {
      this.$store.dispatch('setSending', { sending: true });
      this.$store.dispatch('setFiles', { files });
      this.$store.dispatch('setSourceSending', { source: this.source });
    },
    createObjFiles(file, path, name) {
      if (!this.excludeFileName(name)) {
        const objFile = {
          content: file,
          path,
          name,
          id: this.count.toString(16),
          type: file.type,
        };
        this.count += 1;
        return objFile;
      }
    },
    inputLoadFiles(filesFromInput) {
      const arrayFiles = [];
      for (let i = 0; i < filesFromInput.length; i += 1) {
        const pathFile = filesFromInput[i].webkitRelativePath ? filesFromInput[i].webkitRelativePath : filesFromInput[i].name;
        const objFile = this.createObjFiles(filesFromInput[i], pathFile, filesFromInput[i].name);
        if (objFile) {
          arrayFiles.push(objFile);
        }
      }
      this.storeFiles(arrayFiles);
    },
    manageDataTransfer(dataTransferItems) {
      this.$store.dispatch('setStudyUIDtoSend', { studyUID: '' });
      const arrayPromises = [];
      for (let i = 0; i < dataTransferItems.length; i += 1) {
        const entry = this.determineGetAsEntry(dataTransferItems[i]);
        if (entry && entry.isFile) {
          arrayPromises.push(this.readFilePromise(entry, this));
        } else if (entry && entry.isDirectory) {
          arrayPromises.push(this.readDirectoryPromise(entry, this));
        }
      }

      Promise.all(arrayPromises).then((res) => {
        const filesSend = this.removeNonObjectFiles(this.arrayFlatten(res));
        this.loading = false;
        this.storeFiles(filesSend);
      });
    },
    removeNonObjectFiles(array) {
      return array.filter((val) => (val) !== undefined);
    },
    arrayFlatten(array) {
      return array.reduce((arrayFlat, arrayToFlatten) => arrayFlat.concat(Array.isArray(arrayToFlatten) ? this.arrayFlatten(arrayToFlatten) : arrayToFlatten), []);
    },
    readFilePromise(entry, _this) {
      return new Promise(((resolve) => {
        entry.file((file) => {
          resolve(_this.createObjFiles(file, entry.fullPath, file.name));
        });
      }));
    },
    // https://stackoverflow.com/questions/18815197/javascript-file-dropping-and-reading-directories-asynchronous-recursion
    readDirectoryPromise(readerDir, _this) {
      const reader = readerDir.createReader();
      return new Promise((resolve) => {
        const iterationAttempts = [];
        (function readEntriesRecursive() {
          reader.readEntries((entries) => {
            if (!entries.length) {
              resolve(Promise.all(iterationAttempts));
            } else {
              iterationAttempts.push(Promise.all(entries.map((entry) => {
                if (entry.isFile) {
                  return _this.readFilePromise(entry, _this);
                }
                return _this.readDirectoryPromise(entry, _this);
              })));
              readEntriesRecursive();
            }
          });
        }());
      });
    },
    determineDragAndDropCapable() {
      const div = document.createElement('div');
      let canUpload = this.permissions.add_series;
      if (process.env.VUE_APP_DISABLE_UPLOAD !== undefined) {
        canUpload = canUpload && !process.env.VUE_APP_DISABLE_UPLOAD.includes('true');
      }
      return ((('draggable' in div) || ('ondragstart' in div && 'ondrop' in div)) && 'FormData' in window && 'FileReader' in window && canUpload);
    },
    determineGetAsEntry(item) {
      if (item.getAsEntry !== undefined) {
        return item.getAsEntry();
      } if (item.webkitGetAsEntry !== undefined) {
        return item.webkitGetAsEntry();
      }
      return undefined;
    },
    excludeFileName(name) {
      return this.excludeFiles.indexOf(name) > -1;
    },
  },
};
</script>
