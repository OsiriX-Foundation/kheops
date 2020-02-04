<i18n scoped>
{
  "en": {
    "sharingEnable": "Sharing link enabled",
    "sharingDisable": "No sharing link",
    "sharingTitle": "Sharing URL"
  },
  "fr": {
    "sharingEnable": "Lien de partage actif",
    "sharingDisable": "Pas de lien de partage",
    "sharingTitle": "URL à partager"
  }
}
</i18n>
<template>
  <span>
    <span
      id="sharing-link"
      :text="sharingToken.length > 0 ? $t('sharingEnable') : $t('sharingDisable')"
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
      <template v-slot:title>
        <pop-over-title
          :title="$t('sharingTitle')"
          @cancel="cancelSharingToken"
        />
      </template>
      <sharing-link-popover
        :album-id="albumid"
        :url="urlSharing"
        :tokens="sharingToken"
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
  },
  data() {
    return {
      sharingTokenParams: {
        show: false,
      },
      urlSharing: '',
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
      this.createToken(token).then((res) => {
        const urlSharing = `${process.env.VUE_APP_URL_ROOT}/view/${res.data.access_token}`;
        this.urlSharing = urlSharing;
        this.$emit('gettokens');
      }).catch(() => {
        this.$snotify.error(this.$t('sorryerror'));
      });
    },
  },
};
</script>
