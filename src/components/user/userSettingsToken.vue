<template>
  <div id="userSettingsToken">
    <tokens
      scope="user"
      :write-permission="true"
    />
  </div>
</template>

<script>
import tokens from '@/components/tokens/tokens';

export default {
  name: 'UserSettingsToken',
  components: { tokens },
};
</script>
