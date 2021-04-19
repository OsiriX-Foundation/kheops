<template>
  <span>
    <div
      v-if="comment.mutation_type === 'IMPORT_STUDY'"
      class="flex-grow-1 bd-highlight"
    >
      <span
        v-if="comment.source.report_provider !== undefined"
      >
        {{ $t('comment.newreportstudy', {user: getName(comment.source), reportname: comment.source.report_provider.name, study: comment.study.description ? comment.study.description : comment.study.UID}) }}
      </span>
      <span
        v-else-if="comment.source.capability_token !== undefined"
      >
        <div
          class="text-warning"
        >
          {{ $t('comment.bycapabilitytoken') }}
        </div>
        {{ $t('comment.importstudytoken', {user: getName(comment.source), study: comment.study.description ? comment.study.description : comment.study.UID, title: comment.source.capability_token.title}) }}
      </span>
      <span
        v-else
      >
        <i>{{ comment.source|getUsername }}</i> {{ $t('comment.imported') }} {{ $t('comment.thestudy') }} <b>{{ comment.study.description ? comment.study.description : comment.study.UID }}</b>
      </span>
      <div
        v-if="comment.study.series !== undefined && comment.study.series.length > 0"
      >
        <br> {{ $t('comment.nbseriesinstudy', {nbseries: lengthPresent(comment.study.series), study: comment.study.description ? comment.study.description : comment.study.UID}) }}
        <span
          v-if="lengthNotPresent(comment.study.series) > 0"
          class="text-warning"
        >
          <br> {{ $t('comment.nbnoseriesinstudy', {nbseries: lengthNotPresent(comment.study.series), study: comment.study.description ? comment.study.description : comment.study.UID}) }}
        </span>
        <div>
          <show-hide
            class-show-hide="font-white"
            :text-show="$t('comment.showseries')"
            :text-hide="$t('comment.hideseries')"
          >
            <template
              slot="value-toshow"
            >
              <span
                v-for="serie in comment.study.series"
                :key="serie.id"
              >
                <span
                  v-if="serie.is_present_in_album === false"
                  class="text-warning"
                >
                  <br>{{ $t('comment.theserienotpresentinalbum', {serie: serie.description ? serie.description : serie.UID}) }}
                </span>
                <span
                  v-else
                >
                  <br>{{ $t('comment.theseriepresentinalbum', {serie: serie.description ? serie.description : serie.UID}) }}
                </span>
              </span>
            </template>
          </show-hide>
        </div>
      </div>
    </div>
    <div
      v-if="comment.mutation_type === 'REMOVE_STUDY'"
      class="flex-grow-1 bd-highlight"
    >
      <span
        v-if="comment.source.report_provider !== undefined"
      >
        {{ $t('comment.removereportstudy', {user: getName(comment.source), reportname: comment.source.report_provider.name, study: comment.study.description ? comment.study.description : comment.study.UID}) }}
      </span>
      <span
        v-else-if="comment.source.capability_token !== undefined"
      >
        <div
          class="text-warning"
        >
          {{ $t('comment.bycapabilitytoken') }}
        </div>
        {{ $t('comment.removestudytoken', {user: getName(comment.source), study: comment.study.description ? comment.study.description : comment.study.UID, title: comment.source.capability_token.title}) }}
      </span>
      <span
        v-else
      >
        <i>{{ comment.source|getUsername }}</i> {{ $t('comment.removed') }} {{ $t('comment.thestudy') }} <b>{{ comment.study.description ? comment.study.description : comment.study.UID }}</b>
      </span>
      <div
        v-if="comment.study.series !== undefined && comment.study.series.length > 0"
      >
        <br> {{ $t('comment.nbnoseriesinstudy', {nbseries: lengthNotPresent(comment.study.series), study: comment.study.description ? comment.study.description : comment.study.UID}) }}
        <span
          v-if="lengthPresent(comment.study.series) > 0"
          class="text-warning"
        >
          <br> {{ $t('comment.nbseriesinstudy', {nbseries: lengthPresent(comment.study.series), study: comment.study.description ? comment.study.description : comment.study.UID}) }}
        </span>
        <div>
          <show-hide
            class-show-hide="font-white"
            :text-show="$t('comment.showseries')"
            :text-hide="$t('comment.hideseries')"
          >
            <template
              slot="value-toshow"
            >
              <span
                v-for="serie in comment.study.series"
                :key="serie.id"
              >
                <span
                  v-if="serie.is_present_in_album === true"
                  class="text-warning"
                >
                  <br>{{ $t('comment.theseriepresentinalbum', {serie: serie.description ? serie.description : serie.UID}) }}
                </span>
                <span
                  v-else
                >
                  <br>{{ $t('comment.theserienotpresentinalbum', {serie: serie.description ? serie.description : serie.UID}) }}
                </span>
              </span>
            </template>
          </show-hide>
        </div>
      </div>
    </div>
    <div
      v-if="comment.mutation_type === 'ADD_FAV' && comment.study"
      class=" flex-grow-1 bd-highlight"
    >
      <i>{{ comment.source|getUsername }}</i> {{ $t('comment.addfavorite') }} {{ $t('comment.thestudy') }} <b>{{ comment.study.description ? comment.study.description : comment.study.UID }}</b>
    </div>
    <div
      v-if="comment.mutation_type === 'REMOVE_FAV' && comment.study"
      class=" flex-grow-1 bd-highlight"
    >
      <i>{{ comment.source|getUsername }}</i> {{ $t('comment.removefavorite') }} {{ $t('comment.thestudy') }} <b>{{ comment.study.description ? comment.study.description : comment.study.UID }}</b>
    </div>
  </span>
</template>
<script>
import ShowHide from '@/components/globals/ShowHide';

export default {
  name: 'NotificationsStudy',
  components: { ShowHide },
  props: {
    comment: {
      type: Object,
      required: true,
      default: () => {},
    },
  },
  methods: {
    lengthNotPresent(series) {
      if (series !== undefined) {
        const seriesNotPresent = this.isNotPresent(series);
        return seriesNotPresent.length;
      }
      return 0;
    },
    isNotPresent(series) {
      if (series !== undefined) {
        return series.filter((serie) => serie.is_present_in_album === false);
      }
      return [];
    },
    lengthPresent(series) {
      if (series !== undefined) {
        const seriesPresent = this.isPresent(series);
        return seriesPresent.length;
      }
      return 0;
    },
    isPresent(series) {
      if (series !== undefined) {
        return series.filter((serie) => serie.is_present_in_album === true);
      }
      return [];
    },
    getName(user) {
      return this.$options.filters.getUsername(user);
    },
  },
};
</script>
