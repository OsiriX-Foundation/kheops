<i18n>
	{
		"en": {
		},
		"fr": {
		}
	}
</i18n>

<template>
  <div id="userSettingsToken">
    <tokens scope="user" />
  </div>
</template>

<script>
import tokens from '@/components/tokens/tokens'
export default {
	name: 'UserSettingsToken',
	components: { tokens }
}
</script>

<style scoped>
</style>
