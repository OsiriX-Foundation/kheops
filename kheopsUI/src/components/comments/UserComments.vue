<template>
  <div
    class="card mt-3 ml-sm-5 mr-sm-5"
    :class="(comment.is_private)?'bg-primary':'bg-secondary'"
  >
    <div class="card-header">
      <v-icon
        name="user"
        class="icon-margin-right"
        :class="comment.source.can_access === false ? 'font-neutral' : ''"
      />
      <span
        v-if="currentuserEmail === comment.source.email"
      >
        {{ $t('comment.you') }}
      </span>
      <a
        v-else-if="comment.source.can_access === true && writeComments === true"
        class="font-white"
        :title="comment.source.email"
        @click="clickPrivateUser(comment.source.email)"
      >
        {{ comment.source|getUsername }}
      </a>
      <span
        v-else-if="comment.source.can_access === true"
        color="white"
        :title="comment.source.email"
      >
        {{ comment.source|getUsername }}
      </span>
      <span
        v-else
        class="font-neutral"
        :title="comment.source.email"
      >
        {{ comment.source|getUsername }} - <i>{{ scope === 'album' ? $t('comment.nocommentaccessalbum') : $t('comment.nocommentaccessstudy') }}</i>
      </span>
      <span class="float-right">
        {{ comment.post_date | formatDate }}
      </span>
      <div
        v-if="comment.is_private"
      >
        <b
          v-if="comment.is_private && currentuserEmail !== comment.source.email"
          class="text-warning"
        >
          {{ $t('comment.privatemessagereceive') }}
        </b>
        <b
          v-if="comment.is_private && currentuserEmail !== comment.target.email"
          class="text-warning"
        >
          {{ $t('comment.privatemessagesend') }} {{ comment.target|getUsername }}
        </b>
      </div>
    </div>
    <div
      class="card-body"
    >
      <p
        v-for="(p,pidx) in splitComment(comment.comment)"
        :key="pidx"
        class="my-0 word-break"
      >
        {{ p }}
      </p>
    </div>
  </div>
</template>
<script>

export default {
  name: 'UserComments',
  props: {
    scope: {
      type: String,
      required: true,
      default: '',
    },
    comment: {
      type: Object,
      required: true,
      default: () => {},
    },
    currentuserEmail: {
      type: String,
      required: true,
      default: '',
    },
    writeComments: {
      type: Boolean,
      required: true,
      default: false,
    },
  },
  methods: {
    splitComment(comment) {
      return comment.split('\n');
    },
    clickPrivateUser(email) {
      this.$emit('click-private-user', email);
    },
  },
};
</script>
