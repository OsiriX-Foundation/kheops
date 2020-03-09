<i18n>
{
  "en": {
    "informationtriggeruser": "The below user will be delivered to {url} using the current webhook configuraton.",
    "errortrigger": "You can't redeliver this trigger.",
    "erroruser": "The following user is no longer present in the album :"
  },
  "fr": {
    "informationtriggeruser": "L'utilisateur ci-dessous sera livré à {url} en utilisant la configuration actuelle.",
    "errortrigger": "Vous ne pouvez pas rédéclencher cet évènement.",
    "erroruser": "L'utilisateur suivant n'est plus présent dans l'album :"
  }
}
</i18n>
<template>
  <span>
    <p
      v-if="userNotPresent === false"
      class="word-break"
    >
      {{ $t('informationtriggeruser', { url: url }) }}
    </p>
    <span
      v-if="userNotPresent === true"
    >
      <div
        class="text-warning mb-2"
      >
        {{ $t('errortrigger') }}
      </div>
      <div
        class="text-warning mb-2"
      >
        {{ $t('erroruser') }}
      </div>
    </span>
    <ul
      v-if="trigger.user !== undefined"
    >
      <li v-if="trigger.user.email !== undefined">{{ $t('mail') }} : {{ trigger.user.email }}</li>
      <li v-if="trigger.user.name !== undefined">{{ $t('name') }} : {{ trigger.user.name }}</li>
      <li v-if="trigger.user.sub !== undefined">{{ $t('sub') }} : {{ trigger.user.sub }}</li>
    </ul>
  </span>
</template>
<script>
import { mapGetters } from 'vuex';

export default {
  name: 'TriggerUser',
  components: { },
  props: {
    trigger: {
      type: Object,
      required: true,
      default: () => {},
    },
    url: {
      type: String,
      required: true,
      default: '',
    },
  },
  data() {
    return {
      userNotPresent: false,
    };
  },
  computed: {
    ...mapGetters({
      users: 'albumUsers',
    }),
  },
  created() {
    const albumId = this.$route.params.album_id;
    this.$store.dispatch('getUsersAlbum', { album_id: albumId }).then((res) => {
      this.checkUser();
    }).catch((err) => {
      console.log(err);
    });
  },
  methods: {
    checkUser() {
      const userPresent = this.users.filter((user) => user.sub === this.trigger.user.sub);
      this.userNotPresent = userPresent.length === 0;
      if (this.userNotPresent) {
        this.$emit('missinguser');
      }
    },
  },
};
</script>
