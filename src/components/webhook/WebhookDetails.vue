<template>
  <span
    v-if="Object.entries(webhook).length > 0"
  >
    <div
      class="row word-break my-3 provider-position"
    >
      <h4>
        <button
          type="button"
          class="btn btn-link btn-sm d-md-none"
          @click.stop="done"
        >
          <span>
            <v-icon
              name="arrow-left"
              color="white"
            />
          </span>
        </button>
        {{ webhook.name }}
      </h4>
    </div>
    <div class="row mb-3">
      <div :class="classColRight">
        <b>{{ $t('webhook.urlwebhook') }}</b>
      </div>
      <div :class="classColLeft">
        {{ webhook.url }}
      </div>
    </div>
    <div class="row mb-3">
      <div :class="classColRight">
        <b>{{ $t('webhook.secret') }}</b>
      </div>
      <div :class="classColLeft">
        <span
          v-if="webhook.use_secret === true"
        >
          {{ $t('webhook.secretdefined') }}
        </span>
        <span
          v-else-if="webhook.use_secret === false"
        >
          {{ $t('webhook.nosecret') }}
        </span>
      </div>
    </div>
    <div class="row mb-3">
      <div :class="classColRight">
        <b>{{ $t('webhook.event') }}</b>
      </div>
      <div :class="classColLeft">
        <span
          v-if="webhook.events.length > 0"
        >
          <li
            v-for="event in webhook.events"
            :key="event.id"
          >
            {{ $t(`webhook.${event}`) }}
          </li>
        </span>
        <span
          v-else-if="webhook.events.length === 0"
        >
          {{ $t('webhook.noevent') }}
        </span>
      </div>
    </div>
    <div class="row mb-3">
      <div :class="classColRight">
        <b>{{ $t('webhook.id') }}</b>
      </div>
      <div :class="classColLeft">
        {{ webhook.id }}
      </div>
    </div>
    <div class="row mb-3">
      <div :class="classColRight">
        <b>{{ $t('webhook.state') }}</b>
      </div>
      <div :class="classColLeft">
        <b
          v-if="webhook.enabled === true"
        >
          {{ $t('webhook.webhookenabled') }}
        </b>
        <b
          v-else-if="webhook.enabled === false"
        >
          {{ $t('webhook.webhookdisabled') }}
        </b>
      </div>
    </div>
    <done-delete-button
      class-row="mb-2"
      class-col="offset-md-5 offset-lg-4 col-xs-12 col-sm-12 col-md-5 col-lg-4"
      class-col-warning-remove="offset-md-5 offset-lg-4 col-sm-12 col-md-6 col-lg-7"
      :text-warning-remove="$t('webhook.warningremove')"
      :text-button-done="$t('edit')"
      :loading="onLoading"
      @done="edit"
      @remove="remove"
    />
  </span>
</template>

<script>
import DoneDeleteButton from '@/components/globalbutton/DoneDeleteButton';

export default {
  name: 'WebhookDetails',
  components: { DoneDeleteButton },
  props: {
    albumId: {
      type: String,
      required: true,
      default: '',
    },
    webhook: {
      type: Object,
      required: true,
      default: () => {},
    },
    classColRight: {
      type: String,
      required: false,
      default: '',
    },
    classColLeft: {
      type: String,
      required: false,
      default: '',
    },
    onLoading: {
      type: Boolean,
      required: false,
      default: false,
    },
  },
  data() {
    return {
    };
  },
  computed: {
    webhookId() {
      return this.$route.params.id;
    },
  },
  created() {
  },
  beforeDestroy() {
  },
  methods: {
    done() {
      this.$emit('done');
    },
    edit() {
      this.$emit('edit', this.webhookId);
    },
    remove() {
      this.$emit('remove', this.webhookId);
    },
  },
};
</script>
