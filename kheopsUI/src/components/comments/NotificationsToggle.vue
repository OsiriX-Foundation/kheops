<template>
  <p
    class="col-sm-12 col-md-10 offset-md-1 text-right"
  >
    <label class="mr-2">
      {{ $t('comment.includenotifications') }}
    </label>
    <toggle-button
      v-model="includeNotifications"
      :sync="true"
      :color="{checked: '#5fc04c', unchecked: 'grey'}"
      @change="toggleChange"
    />
  </p>
</template>
<script>
export default {
  name: 'NotificationsToggle',
  data() {
    return {
      includeNotifications: false,
    };
  },
  methods: {
    toggleChange() {
      this.$emit('change', this.includeNotifications);
    },
  },
};
</script>
