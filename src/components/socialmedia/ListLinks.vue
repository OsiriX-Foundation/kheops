<template>
  <span>
    <twitter-link
      :albumid="albumid"
      :tokens="albumTokens"
      @revoketokens="revokeTokens"
      @gettokens="getTokens"
    />
    <sharing-link
      :albumid="albumid"
      :tokens="albumTokens"
      @revoketokens="revokeTokens"
      @gettokens="getTokens"
    />
  </span>
</template>

<script>
import { mapGetters } from 'vuex';
import SharingLink from '@/components/socialmedia/SharingLink';
import TwitterLink from '@/components/socialmedia/TwitterLink';

export default {
  name: 'ListLinks',
  components: { SharingLink, TwitterLink },
  props: {
    albumid: {
      type: String,
      required: true,
      default: null,
    },
  },
  computed: {
    ...mapGetters({
      albumTokens: 'albumTokens',
      validParamsToken: 'validParamsToken',
    }),
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
  },
};
</script>
