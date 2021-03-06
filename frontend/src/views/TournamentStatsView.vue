<template>
  <div v-if="stats != null" class="stats-container">
    <div class="items">
      <div
        class="icon-wrapper"
        ref="totalNumberCreatedTournaments"
        data-cy="totalNumberCreatedTournaments"
      >
        <animated-number :number="stats.totalNumberCreatedTournaments" />
      </div>
      <div class="project-name">
        <p>Total Tournaments Created</p>
      </div>
    </div>
    <div class="items">
      <div
        class="icon-wrapper"
        ref="totalNumberEnrolledTournaments"
        data-cy="totalNumberEnrolledTournaments"
      >
        <animated-number :number="stats.totalNumberEnrolledTournaments" />
      </div>
      <div class="project-name">
        <p>Total Tournaments Enrolled</p>
      </div>
    </div>
	  <v-card-text style="height: 50px; position: relative">
		  <v-tooltip v-if="stats.privateTournamentsStats" bottom>
			  <template v-slot:activator="{ on }">
				  <v-btn
						  absolute
						  dark
						  fab
						  top
						  right
						  color="red"
						  v-on="on"
						  @click="changeTournamentsStatsPrivacy"
						  data-cy="publicTournamentsStatsBtn"
				  >
					  <v-icon class="mr-2">mdi-eye-off</v-icon>
				  </v-btn>
			  </template>
			  <span>Click here to make tournaments stats public</span>
		  </v-tooltip>
		  <v-tooltip v-else bottom>
			  <template v-slot:activator="{ on }">
				  <v-btn
						  absolute
						  dark
						  fab
						  top
						  right
						  color="green"
						  v-on="on"
						  @click="changeTournamentsStatsPrivacy"
						  data-cy="privateTournamentsStatsBtn"
				  >
					  <v-icon class="mr-2">mdi-eye</v-icon>
				  </v-btn>
			  </template>
			  <span>Click here to make tournaments stats private</span>
		  </v-tooltip>
	  </v-card-text>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';
import StudentStats from '@/models/statement/StudentStats';
import RemoteServices from '@/services/RemoteServices';
import AnimatedNumber from '@/components/AnimatedNumber.vue';

@Component({
  components: { AnimatedNumber }
})
export default class TournamentsStatsView extends Vue {
  @Prop(StudentStats) readonly stats!: StudentStats;

    async changeTournamentsStatsPrivacy() {
        await this.$store.dispatch('loading');
        try {
            await RemoteServices.changeTournamentsStatsPrivacy();
            this.stats.privateTournamentsStats = !this.stats.privateTournamentsStats;
        } catch (error) {
            await this.$store.dispatch('error', error);
        }
        await this.$store.dispatch('clearLoading');
    }
}
</script>

<style lang="scss" scoped>
.stats-container {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  justify-content: center;
  align-items: stretch;
  align-content: center;
  height: 100%;

  .items {
    background-color: rgba(255, 255, 255, 0.75);
    color: #1976d2;
    border-radius: 5px;
    flex-basis: 25%;
    margin: 20px;
    cursor: pointer;
    transition: all 0.6s;
  }
}

.icon-wrapper,
.project-name {
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-wrapper {
  font-size: 100px;
  transform: translateY(0px);
  transition: all 0.6s;
}

.icon-wrapper {
  align-self: end;
}

.project-name {
  align-self: start;
}
.project-name p {
  font-size: 24px;
  font-weight: bold;
  letter-spacing: 2px;
  transform: translateY(0px);
  transition: all 0.5s;
}

.items:hover {
  border: 3px solid black;

  & .project-name p {
    transform: translateY(-10px);
  }
  & .icon-wrapper i {
    transform: translateY(5px);
  }
}
</style>
