<i18n>
{
  "en": {
    "new_series": "new serie",
    "new_user": "new user",
    "activate": "Trigger by adding {event}",
    "manualtrigger": "Manual trigger",
    "attempt":"Attempt",
    "status": "Status",
    "date": "Date",
    "-1": "An error occur",
    "redeliver": "Redeliver",
    "studies": "Study"
  },
  "fr": {
    "new_series": "nouvelles série",
    "new_user": "nouvel utilisateur",
    "activate": "Déclenché par l'ajout de {event}",
    "manualtrigger": "Déclenchement manuel",
    "attempt":"Tentative",
    "status": "Status",
    "date": "Date",
    "-1": "Une erreur est survenue",
    "redeliver": "Redéclencher",
    "studies": "Etude"
  }
}
</i18n>
<template>
  <span>
    <div class="d-flex mb-2">
      <div class="">{{ $t('activate', {event: $t(trigger.event)}) }}
        <span
          v-if="trigger.event === 'new_series'"
        >
          -
          <router-link
            :to="{ name: 'album', query: { StudyInstanceUID: trigger.study.study_uid }}"
            taget="_blank"
            active-class="active"
          >
            {{ $t('studies') }}
          </router-link>
        </span>
        <span
          v-if="trigger.event === 'new_user'"
        >
          - {{ trigger.user.email }}
        </span>
      </div>
      <div class="ml-auto">
        <button
          class="btn btn-sm btn-primary"
          :disabled="disabledTrigger"
          @click="triggerWebhook()"
        >
          {{ $t('redeliver') }}
        </button>
      </div>
    </div>
    <p
      v-if="trigger.is_manual_trigger === true"
    >
      {{ $t('manualtrigger') }}
    </p>
    <span
      class="d-flex flex-wrap flex-row bd-highlight mb-3"
    >
      <span
        v-for="attempt in trigger.attempts"
        :key="attempt.id"
        class="p-2 bd-highlight"
      >
        <b>{{ $t('attempt') }} {{ attempt.attempt }}</b> <br>
        {{ $t('status') }}: {{ $t(attempt.status) }}<br>
        {{ $t('date') }}: {{ attempt.time | formatDateTimeDetails }}<br>
        <br>
      </span>
    </span>
  </span>
</template>

<script>
import httpoperations from '@/mixins/httpoperations';
import { HTTP } from '@/router/http';

export default {
  name: 'DetailAttempts',
  components: { },
  props: {
    trigger: {
      type: Object,
      required: true,
      default: () => {},
    },
  },
  data() {
    return {
      disabledTrigger: false,
    };
  },
  computed: {
    albumId() {
      return this.$route.params.album_id;
    },
    webhookId() {
      return this.$route.params.id;
    },
  },
  created() {
  },
  beforeDestroy() {
  },
  methods: {
    triggerWebhook() {
      this.disabledTrigger = true;
      const formData = this.generateFormData();
      const url = `albums/${this.albumId}/webhooks/${this.webhookId}/trigger`;
      HTTP.post(url, formData).then(() => {
        this.$emit('manualtrigger');
        this.disabledTrigger = false;
      }).catch((err) => {
        this.$snotify.error(`${this.$t('sorryerror')} ${err.request !== undefined && err.request.status !== undefined ? `[${err.request.status}]` : ''}`);
        this.disabledTrigger = false;
      });
    },
    generateFormData() {
      let formData = {};
      if (this.trigger.event === 'new_series' && this.trigger.study !== undefined) {
        formData = this.generateStudyFormData(this.trigger);
      }
      if (this.trigger.event === 'new_user' && this.trigger.user !== undefined) {
        formData = this.generateUserFormData(this.trigger);
      }
      formData.event = this.trigger.event;
      return httpoperations.getFormData(formData);
    },
    generateStudyFormData(trigger) {
      const { study } = trigger;
      const formData = {
        StudyInstanceUID: study.study_uid,
        SeriesInstanceUID: study.series.map((serie) => serie.series_uid),
      };
      return formData;
    },
    generateUserFormData(trigger) {
      const { user } = trigger;
      const formData = {
        user: user.email !== undefined && user.email !== '' ? user.email : user.sub,
      };
      return formData;
    },
  },
};
</script>
