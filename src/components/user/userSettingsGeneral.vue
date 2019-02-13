<i18n>
{
	"en": {
		"language": "language"
	},
	"fr": {
		"language": "langue"
	}
}
</i18n>

<template>
<div id = 'userSettingsGeneral'>
	<form>
		<dl class = 'row'>
			<dt class = 'col-md-3 col-xs-12 text-right'>{{$t('language')}}</dt>
			<dd class = 'col-md-9 col-xs-12 text-left'>
				<select v-model='lang' class = 'form-control'>
					<option value='en'>English</option>
					<option value='fr'>Fançais</option>
				</select>
			</dd>
		</dl>
	</form>
</div>
</template>

<script>
export default{
	name: 'userSettingsGeneral',
	data () {
		return {
		}
	},
	computed: {
		lang:  {
			get () {
				return this.$i18n.locale				
			},
			set (value) {
				this.$root.$i18n.locale = value	
			}
		}		
	}
}
</script>

<style scoped>
dt {
	text-transform: capitalize;
}
</style>