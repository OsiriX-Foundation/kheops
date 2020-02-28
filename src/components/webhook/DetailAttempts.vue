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
    "studies": "Study",
    "informationtrigger": "The below series will be delivered to {url} using the current webhook configuraton.",
    "informationtriggeruser": "The below user will be delivered to {url} using the current webhook configuraton.",
    "mail": "Mail",
    "lastname": "Last name",
    "firstname": "First name",
    "sub": "Sub"
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
    "studies": "Etude",
    "informationtrigger": "Les séries ci-dessous seront livrées à {url} en utilisant la configuration actuelle.",
    "informationtriggeruser": "L'utilisateur ci-dessous sera livré à {url} en utilisant la configuration actuelle.",
    "mail": "Email",
    "lastname": "Nom de famille",
    "firstname": "Prénom",
    "sub": "Sub"
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
          @click="showTrigger"
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
    <b-modal
      id="triggerModal"
      ref="triggerModal"
      header-class="bg-primary"
      title-class="word-break"
      body-class="bg-secondary"
      centered
      hide-footer
      size="lg"
    >
      <template v-slot:modal-title>
        {{ $t('manualtrigger') }}
      </template>
      <span
        v-if="trigger.event === 'new_series'"
      >
        <p>
          {{ $t('informationtrigger', { url: webhook.url }) }}
        </p>
        <details-series
          v-if="trigger.event === 'new_series'"
          class-col="col-2 col-sm-3 col-md-4 col-lg-3 col-xl-2 mb-5"
          class-row="serie-section card-main mb-3"
          height="75"
          width="75"
          :serie-uids="seriesUIDS"
          :study-uid="trigger.study.study_uid"
          :source="source"
        />
      </span>
      <span
        v-if="trigger.event === 'new_user'"
      >
        <p>
          {{ $t('informationtriggeruser', { url: webhook.url }) }}
        </p>
        <ul
          v-if="trigger.user !== undefined"
        >
          <li v-if="trigger.user.email !== undefined">{{ $t('mail') }} : {{ trigger.user.email }}</li>
          <li v-if="trigger.user.last_name !== undefined">{{ $t('lastname') }} : {{ trigger.user.last_name }}</li>
          <li v-if="trigger.user.first_name !== undefined">{{ $t('firstname') }} : {{ trigger.user.first_name }}</li>
          <li v-if="trigger.user.sub !== undefined">{{ $t('sub') }} : {{ trigger.user.sub }}</li>
        </ul>
      </span>
      <div
        class="text-center"
      >
        <button
          class="btn btn-primary "
          :disabled="disabledTrigger"
          @click="triggerWebhook"
        >
          {{ $t('redeliver') }}
        </button>
      </div>
    </b-modal>
  </span>
</template>

<script>
import { mapGetters } from 'vuex';
import httpoperations from '@/mixins/httpoperations';
import DetailsSeries from '@/components/series/DetailsSeries';
import { HTTP } from '@/router/http';

export default {
  name: 'DetailAttempts',
  components: { DetailsSeries },
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
    ...mapGetters({
      webhook: 'webhook',
    }),
    albumId() {
      return this.$route.params.album_id;
    },
    webhookId() {
      return this.$route.params.id;
    },
    source() {
      return {
        key: 'album',
        value: this.$route.params.album_id,
      };
    },
    seriesUIDS() {
      if (this.trigger.event === 'new_series' && this.trigger.study !== undefined && this.trigger.study.series !== undefined) {
        return this.trigger.study.series.map((serie) => serie.series_uid);
      }
      return [];
    },
  },
  created() {
  },
  beforeDestroy() {
  },
  methods: {
    showTrigger() {
      this.$refs.triggerModal.show();
    },
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
