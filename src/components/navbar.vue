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
		"favorites": "Favorits",
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
					<a class="nav-link active" href="/inbox"><v-icon name='bars'/>{{ $t("inbox") }}</a>
				</li>
				<li class="nav-item">
					<a class="nav-link" href="/albums"><v-icon name='book' />{{ $t("albums") }}</a>
				</li>
				<li class="nav-item">
					<a class="nav-link" href="/favorites"><v-icon name='star' />{{ $t("favorites") }}</a>
				</li>
				<li class="nav-item">
					<a class="nav-link disabled" href="/recents"><v-icon name='clock-o'/>{{ $t("recents") }}</a>
				</li>
			</ul>
		</div>
	</div>
</template>

<script>
export default {
  name: 'navBar'
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