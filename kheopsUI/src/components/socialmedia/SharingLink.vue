<template>
  <span>
    <span
      id="sharing-link"
      :text="sharingToken.length > 0 ? $t('sharinglink.sharingEnable') : $t('sharinglink.sharingDisable')"
      class="btn btn-link p-0"
      @click.stop="sharingTokenParams.show = !sharingTokenParams.show"
    >
      <v-icon
        name="link"
        :color="(sharingToken.length > 0) ? 'white' : 'grey'"
        scale="2"
      />
    </span>
    <b-popover
      v-if="sharingTokenParams.show"
      target="sharing-link"
      :show.sync="sharingTokenParams.show"
      placement="auto"
    >
      <template #title>
        <pop-over-title
          :title="$t('sharinglink.sharingTitle')"
          @cancel="cancelSharingToken"
        />
      </template>
      <sharing-link-popover
        :album-id="albumid"
        :url="urlSharing"
        :tokens="sharingToken"
        :loading="waitCreate || loading"
        @cancel="cancelSharingToken"
        @revoke="revokeSharingTokens"
        @create="createSharingToken"
      />
    </b-popover>
  </span>
</template>
<script>
import moment from 'moment';
import PopOverTitle from '@/components/socialmedia/PopOverTitle';
import SharingLinkPopover from '@/components/socialmedia/SharingLinkPopover';

export default {
  name: 'SharingLink',
  components: { PopOverTitle, SharingLinkPopover },
  props: {
    albumid: {
      type: String,
      required: true,
      default: null,
    },
    tokens: {
      type: Array,
      required: true,
      default: () => [],
    },
    loading: {
      type: Boolean,
      required: false,
      default: false,
    },
  },
  data() {
    return {
      sharingTokenParams: {
        show: false,
      },
      urlSharing: '',
      waitCreate: false,
    };
  },
  computed: {
    sharingToken() {
      return this.tokens.filter((token) => token.title.includes('sharing_link') && moment(token.expiration_time) > moment() && !token.revoked);
    },
  },
  methods: {
    createToken(token) {
      return this.$store.dispatch('createToken', { token });
    },
    cancelSharingToken() {
      this.sharingTokenParams.show = false;
    },
    revokeSharingTokens(tokens) {
      this.cancelSharingToken();
      this.urlSharing = '';
      this.$emit('revoketokens', tokens);
    },
    createSharingToken(token) {
      this.waitCreate = true;
      this.createToken(token).then((res) => {
        const urlSharing = `${process.env.VUE_APP_URL_ROOT}/view/${res.data.access_token}`;
        this.urlSharing = urlSharing;
        this.$emit('gettokens');
        this.waitCreate = false;
      }).catch(() => {
        this.$snotify.error(this.$t('sorryerror'));
        this.waitCreate = false;
      });
    },
  },
};
</script>
