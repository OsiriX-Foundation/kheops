/* eslint-disable */
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
					<a class="nav-link" :class='(activePath=="inbox")?"active":""' href="/inbox"><v-icon name='bars'/>{{ $t("inbox") }}</a>
				</li>
				<li class="nav-item">
					<a class="nav-link" :class='(activePath=="albums")?"active":""' href="/albums"><v-icon name='book' />{{ $t("albums") }}</a>
				</li>
				<li class="nav-item">
					<a class="nav-link" :class='(activePath=="favorites")?"active":""' href="/favorites"><v-icon name='star' />{{ $t("favorites") }}</a>
				</li>
				<li class="nav-item">
					<a class="nav-link disabled" :class='(activePath=="recents")?"active":""' href="/recents"><v-icon name='clock-o'/>{{ $t("recents") }}</a>
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
		'$route' (to, from){
			console.log(to.path.split("/")[1]);
			this.activePath = to.path.split("/")[1];
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