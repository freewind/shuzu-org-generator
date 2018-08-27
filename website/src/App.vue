<template>
    <div>
        <div>Demo Driven Learning</div>
        <input type="text" v-model="keyword" autofocus
               v-on:keydown.down="selectNextDemo()"
               v-on:keydown.up="selectPrevDemo()"
               v-on:keydown.enter="openDemo()"
        />
        <div>Search in {{totalDemoCount}} complete small demos</div>
        <hr/>
        <ul>
            <li v-for="demo in filteredDemos" v-bind:key="demo.name"
                v-bind:class="{ 'selected-demo': demo === currentDemo}">
                <HighlightMatch v-bind:text="demo.name" v-bind:keywords="standardKeywords"/>
                <span>{{ demo.description }}</span>
            </li>
        </ul>
    </div>
</template>

<script lang="ts">
    import {Component, Vue, Watch} from 'vue-property-decorator'
    import _allDemos from '../resources/live-search.json'
    import {matchesKeywords, splitKeywords} from './keywords-matching'
    import HighlightMatch from './components/HighlightMatch'

    type Demo = {
        name: string,
        description?: string
    }

    const allDemos = <Demo[]>_allDemos

    @Component({
        components: {
            HighlightMatch
        }
    })
    export default class App extends Vue {
        readonly totalDemoCount: number = allDemos.length
        keyword: string = ''

        // update when keyword changes
        standardKeywords: string[] = []
        filteredDemos: Demo[] = []
        currentDemo: Demo | null = null

        @Watch('keyword')
        updateEverything() {
            this.updateKeywords()
            this.updateFilteredDemos()
            this.updateCurrentDemo()
        }

        private updateKeywords() {
            this.standardKeywords = splitKeywords(this.keyword)
        }

        private updateFilteredDemos() {
            if (this.standardKeywords.length === 0) {
                this.filteredDemos = []
            } else {
                this.filteredDemos = allDemos.filter(demo => matchesKeywords(demo.name, this.standardKeywords))
            }
        }

        private updateCurrentDemo() {
            if (this.filteredDemos.length > 0) {
                this.currentDemo = this.filteredDemos[0]
            } else {
                this.currentDemo = null
            }
        }

        selectNextDemo() {
            if (this.currentDemo !== null) {
                let index = this.filteredDemos.indexOf(this.currentDemo) + 1
                if (index >= this.filteredDemos.length) {
                    index = 0
                }
                this.currentDemo = this.filteredDemos[index]
            }
        }

        selectPrevDemo() {
            if (this.currentDemo !== null) {
                const demos = this.filteredDemos
                let index = this.filteredDemos.indexOf(this.currentDemo) - 1
                if (index < 0) {
                    index = demos.length - 1
                }
                this.currentDemo = demos[index]
            }
        }

        openDemo() {
            if (this.currentDemo !== null) {
                const url = 'https://github.com/freewind-demos/' + this.currentDemo.name
                window.open(url)
            }
        }
    }
</script>

<style scoped>
    .selected-demo {
        background-color: #DDD;
    }
</style>
