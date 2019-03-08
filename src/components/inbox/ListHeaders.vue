<i18n>
{
	"en": {
		"selectednbstudies": "{count} study is selected | {count} studies are selected",
		"addalbum": "Add to an album"
	},
	"fr": {
		"selectednbstudies": "{count} étude est sélectionnée | {count} études sont sélectionnées",
		"addalbum": "Ajouter à un album"
	}
}
</i18n>

<template>
  <div>
    <!--button Study selected -->
    <div class="container-fluid my-3 selection-button-container">
      <span
        v-if="selectedStudiesNb"
        class="float-left"
      >
        <span>{{ $tc("selectednbstudies",selectedStudiesNb,{count: selectedStudiesNb}) }}</span>
        <button
          v-if="!albumId"
          type="button"
          class="btn btn-link btn-sm text-center"
          @click.stop="form_send_study=!form_send_study"
        >
          <span>
            <v-icon
              class="align-middle"
              name="paper-plane"
            />
          </span><br>
          {{ $t("send") }}
        </button>
        <b-dropdown
          v-if="!albumId || (album.send_series || album.is_admin)"
          variant="link"
          size="sm"
          no-caret
        >
          <template slot="button-content">
            <span>
              <v-icon
                class="align-middle"
                name="book"
              />
            </span><br>{{ $t("addalbum") }}
          </template>
          <b-dropdown-item
            v-for="allowedAlbum in allowedAlbums"
            :key="allowedAlbum.id"
            @click.stop="addToAlbum(allowedAlbum.album_id)"
          >
            {{ allowedAlbum.name }}
          </b-dropdown-item>
        </b-dropdown>
      </span>
    </div>
    <br>
    <form-get-user
      v-if="form_send_study && selectedStudiesNb"
      @get-user="sendToUser"
      @cancel-user="form_send_study=false"
    />
  </div>
</template>

<script>
import ToggleButton from 'vue-js-toggle-button'
import Vue from 'vue'
import formGetUser from '@/components/user/getUser'

Vue.use(ToggleButton)

export default {
	name: 'Studies',
	components: { formGetUser },
	props: {
		studies: {
			type: Array,
			required: true,
			default: () => ([])
		},
		albumId: {
			type: String,
			required: true,
			default: ''
		},
		albums: {
			type: Array,
			required: true,
			default: () => ({})
		},
		album: {
			type: Object,
			required: false,
			default: () => ({})
		}
	},
	data () {
		return {
			send: {
				expected: 0,
				count: 0
			},
			form_send_study: false
		}
	},
	computed: {
		selectedStudiesNb () {
			return _.filter(this.studies, s => { return s.is_selected === true }).length
		},
		allowedAlbums () {
			return _.filter(this.albums, a => { return (a.add_series || a.is_admin) && this.albumId !== a.album_id })
		}
	},

	watch: {
	},

	created () {
	},
	mounted () {
	},
	methods: {
		sendToUser (userSub) {
			console.log(this.studies)
			let studies = _.filter(this.studies, s => { return s.is_selected })
			let studyIds = []; let seriesIds = []
			_.forEach(studies, s => {
				let selectedSeries = _.filter(s.series, oneSeries => { return oneSeries.is_selected })
				if (selectedSeries.length === s.series.length) studyIds.push(s.StudyInstanceUID[0])
				else {
					_.forEach(selectedSeries, oneSeries => {
						seriesIds.push({
							StudyInstanceUID: s.StudyInstanceUID[0],
							SeriesInstanceUID: oneSeries.SeriesInstanceUID[0]
						})
					})
				}
			})
			if (studyIds.length || seriesIds.length) {
				this.$store.dispatch('sendStudies', { StudyInstanceUIDs: studyIds, SeriesInstanceUIDs: seriesIds, user: userSub }).then(res => {
					this.$snotify.success(`${res.success} ${this.$t('studiessharedsuccess')}`)
					if (res.error) this.$snotify.error(`${res.error} ${this.$t('studiessharederror')}`)
				})
			}
		}
	}
}

</script>
