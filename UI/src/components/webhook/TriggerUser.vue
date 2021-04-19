<template>
  <span>
    <p
      v-if="userNotPresent === false"
      class="word-break"
    >
      {{ $t('webhook.informationtriggeruser', { url: url }) }}
    </p>
    <span
      v-if="userNotPresent === true"
    >
      <div
        class="text-warning mb-2"
      >
        {{ $t('webhook.errortrigger') }}
      </div>
      <div
        class="text-warning mb-2"
      >
        {{ $t('webhook.erroruser') }}
      </div>
    </span>
    <ul
      v-if="trigger.user !== undefined"
    >
      <li v-if="trigger.user.email !== undefined">{{ $t('user.mail') }} : {{ trigger.user.email }}</li>
      <li v-if="trigger.user.name !== undefined">{{ $t('user.name') }} : {{ trigger.user.name }}</li>
      <li v-if="trigger.user.sub !== undefined">{{ $t('user.sub') }} : {{ trigger.user.sub }}</li>
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
    this.$store.dispatch('getUsersAlbum', { album_id: albumId }).then(() => {
      this.checkUser();
    }).catch(() => {
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
