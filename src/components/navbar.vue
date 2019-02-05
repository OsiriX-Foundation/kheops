<i18n>

{
	"en" : {
		"inbox": "Inbox",
		"albums": "Albums",
		"favorites": "Favorites",
		"recents": "Recents"
	},
	"fr": {
		"inbox": "Boîte de réception",
		"albums": "Albums",
		"favorites": "Favoris",
		"recents": "Récents"

	}
}

</i18n>

<template>
	<div class = 'container' id = 'navbar_container'>
		<div class = 'buttons-container'>
			<p></p>
		</div>
	
		<div class = 'nav-container'>
			<ul class="nav nav-pills nav-fill">
				<li class="nav-item">
					<router-link to="/inbox" class="nav-link" active-class="active"><v-icon name='bars'/>{{ $t("inbox") }}</router-link>
				</li>
				<li class="nav-item">
					<router-link to="/albums" class="nav-link" active-class="active"><v-icon name='book'/>{{ $t("albums") }}</router-link>
				</li>
				<li class="nav-item">
					<router-link to="/favorites" class="nav-link" active-class="active"><v-icon name='star'/>{{ $t("favorites") }}</router-link>
				</li>
				<li class="nav-item">
					<router-link to="/recents" class="nav-link" active-class="active"><v-icon name='clock'/>{{ $t("recents") }}</router-link>
				</li>
			</ul>
		</div>
	</div>
</template>

<script>
export default {
  name: 'navBar',
	data () {
		return {
			activePath: 'inbox'
		}
	},
	watch: {
		'$route' (to) {
			this.activePath = to.path.split('/')[1]
		}
	}
}

</script>

<style>
#navbar_container{
	margin-top: 150px;
}
svg.fa-icon{
	margin-right: 10px;
}
</style>