<i18n scoped>
{
  "en": {
    "twitterEnable": "Twitter link enabled",
    "twitterDisable": "No twitter link",
    "sharingEnable": "Sharing link enabled",
    "sharingDisable": "No sharing link",
    "sharingTitle": "Sharing URL",
    "twitterText": "My KHEOPS shared album. {link} #KHEOPS"
  },
  "fr": {
    "twitterEnable": "Lien twitter actif",
    "twitterDisable": "Pas de lien twitter",
    "sharingEnable": "Lien de partage actif",
    "sharingDisable": "Pas de lien de partage",
    "sharingTitle": "URL à partager",
    "twitterText": "Mon album KHEOPS partagé. {link} #KHEOPS"
  }
}
</i18n>
<template>
  <span>
    <span
      id="twitter-link"
      :text="twitterToken.length > 0 ? $t('twitterEnable') : $t('twitterDisable')"
      class="pointer"
      @click.stop="toggleTwitter(albumid)"
    >
      <v-icon
        name="twitter"
        :color="(twitterToken.length > 0) ? '#1da1f2' : '#657786'"
        scale="3"
      />
    </span>
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
      v-if="showRevokeTwitter"
      target="twitter-link"
      :show="showRevokeTwitter"
      placement="auto"
    >
      <template v-slot:title>
        <pop-over-title
          title="Twitter"
          @cancel="showRevokeTwitter = false"
        />
      </template>
      <twitter-link-popover
        :tokens="twitterToken"
        @revoke="revokeTwitterTokens"
        @cancel="showRevokeTwitter = false"
      />
    </b-popover>
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
import { mapGetters } from 'vuex';
import moment from 'moment';
import PopOverTitle from '@/components/socialmedia/PopOverTitle';
import SharingLinkPopover from '@/components/socialmedia/SharingLinkPopover';
import TwitterLinkPopover from '@/components/socialmedia/TwitterLinkPopover';

export default {
  name: 'ListLinks',
  components: { PopOverTitle, SharingLinkPopover, TwitterLinkPopover },
  props: {
    albumid: {
      type: String,
      required: true,
      default: null,
    },
  },
  data() {
    return {
      twitterTokenParams: {
        title: 'twitter_link',
        scope_type: 'album',
        album: '',
        read_permission: true,
        write_permission: false,
        download_permission: true,
        appropriate_permission: false,
        expiration_time: '',
      },
      showRevokeTwitter: false,
      sharingTokenParams: {
        show: false,
      },
      urlSharing: '',
    };
  },
  computed: {
    ...mapGetters({
      albumTokens: 'albumTokens',
    }),
    twitterToken() {
      return this.albumTokens.filter((token) => token.title.includes('twitter_link') && moment(token.expiration_time) > moment() && !token.revoked);
    },
    sharingToken() {
      return this.albumTokens.filter((token) => token.title.includes('sharing_link') && moment(token.expiration_time) > moment() && !token.revoked);
    },
  },
  created() {
  },
  methods: {
    getTokens() {
      const queries = {
        valid: this.validParamsToken,
        album: this.albumid,
      };
      return this.$store.dispatch('getAlbumTokens', { queries });
    },
    revokeTokens(tokens) {
      tokens.forEach((token) => {
        this.$store.dispatch('revokeToken', { token_id: token.id }).then((res) => {
          if (res.status === 200) {
            this.getTokens();
          }
        }).catch(() => {
          this.$snotify.error(this.$t('sorryerror'));
        });
      });
    },
    revokeTwitterTokens(tokens) {
      this.showRevokeTwitter = false;
      this.revokeTokens(tokens);
    },
    toggleTwitter(albumID) {
      if (this.twitterToken.length === 0) {
        this.createTwitterToken(albumID);
      } else {
        this.showRevokeTwitter = this.twitterToken.length > 0 && !this.showRevokeTwitter;
      }
    },
    createToken(token) {
      return this.$store.dispatch('createToken', { token });
    },
    createTwitterToken(albumID) {
      this.twitterTokenParams.album = albumID;
      this.twitterTokenParams.expiration_time = moment().add(100, 'Y').format();
      const twitterWindow = window.open('', 'twitter');
      this.createToken(this.twitterTokenParams).then((res) => {
        const urlTwitter = 'https://twitter.com/intent/tweet';
        const link = `${process.env.VUE_APP_URL_ROOT}/view/${res.data.access_token}`;
        const urlSharing = this.$t('twitterText', { link });
        const queries = `?text=${encodeURIComponent(urlSharing)}`;
        twitterWindow.location.href = urlTwitter + queries;
      }).catch(() => {
        this.$snotify.error(this.$t('sorryerror'));
      });
    },
    cancelSharingToken() {
      this.sharingTokenParams.show = false;
    },
    revokeSharingTokens(tokens) {
      this.cancelSharingToken();
      this.urlSharing = '';
      this.revokeTokens(tokens);
    },
    createSharingToken(token) {
      this.createToken(token).then((res) => {
        const urlSharing = `${process.env.VUE_APP_URL_ROOT}/view/${res.data.access_token}`;
        this.urlSharing = urlSharing;
      }).catch(() => {
        this.$snotify.error(this.$t('sorryerror'));
      });
    },
  },
};
</script>
