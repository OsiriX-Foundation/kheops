<template>
  <div
    class="card col-sm-10 offset-sm-2 bg-secondary mt-3 ml-sm-5 mr-sm-5"
  >
    <div class="card-header">
      <div class="bd-highlight">
        <i>{{ comment.post_date | formatDate }} </i>
      </div>
    </div>
    <div class="card-body">
      <notifications-study
        :comment="comment"
      />
      <notifications-series
        :comment="comment"
      />
      <notifications-user
        :comment="comment"
      />
      <notifications-album
        :comment="comment"
      />
      <notifications-provider
        :comment="comment"
      />
      <notifications-webhook
        :comment="comment"
      />
    </div>
  </div>
</template>
<script>
import NotificationsStudy from '@/components/comments/NotificationsStudy';
import NotificationsSeries from '@/components/comments/NotificationsSeries';
import NotificationsUser from '@/components/comments/NotificationsUser';
import NotificationsAlbum from '@/components/comments/NotificationsAlbum';
import NotificationsProvider from '@/components/comments/NotificationsProvider';
import NotificationsWebhook from '@/components/comments/NotificationsWebhook';

export default {
  name: 'Notifications',
  components: {
    NotificationsStudy, NotificationsSeries, NotificationsUser, NotificationsAlbum, NotificationsProvider, NotificationsWebhook,
  },
  props: {
    comment: {
      type: Object,
      required: true,
      default: () => {},
    },
  },
};
</script>
