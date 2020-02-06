<i18n scoped>
{
  "en": {
    "twitterEnable": "Twitter link enabled",
    "twitterDisable": "No twitter link",
    "twitterText": "My KHEOPS shared album. {link} #KHEOPS"
  },
  "fr": {
    "twitterEnable": "Lien twitter actif",
    "twitterDisable": "Pas de lien twitter",
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
  </span>
</template>
<script>
import moment from 'moment';
import PopOverTitle from '@/components/socialmedia/PopOverTitle';
import TwitterLinkPopover from '@/components/socialmedia/TwitterLinkPopover';

export default {
  name: 'TwitterLink',
  components: { PopOverTitle, TwitterLinkPopover },
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
      urlSharing: '',
    }
  },
  computed: {
    twitterToken() {
      return this.tokens.filter((token) => token.title.includes('twitter_link') && moment(token.expiration_time) > moment() && !token.revoked);
    },
  },
  methods: {
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
        this.$emit('gettokens');
      }).catch(() => {
        this.$snotify.error(this.$t('sorryerror'));
      });
    },
    revokeTwitterTokens(tokens) {
      this.showRevokeTwitter = false;
      this.$emit('revoketokens', tokens);
    },
    toggleTwitter(albumID) {
      if (this.twitterToken.length === 0) {
        this.createTwitterToken(albumID);
      } else {
        this.showRevokeTwitter = this.twitterToken.length > 0 && !this.showRevokeTwitter;
      }
    },
  },
};
</script>
