<template>
    <div class="live-search">
        <div class="search-input">
            <input type="text" v-model="keyword" autofocus
                   v-on:keydown.down="selectNextDemo()"
                   v-on:keydown.up="selectPrevDemo()"
                   v-on:keydown.enter="openDemo()"
            />
        </div>
        <div class="search-tips">Search in {{totalDemoCount}} small but complete demos</div>
        <div class="filtered-demos">
            <ul>
                <li v-for="demo in filteredDemos" v-bind:key="demo.name"
                    v-bind:class="{ 'selected-demo': demo === currentDemo}">
                    <a v-bind:href="demoPath(demo)" target="_blank">
                        <HighlightMatch v-bind:text="demo.name" v-bind:keywords="standardKeywords"/>
                    </a>
                </li>
            </ul>
        </div>
    </div>
</template>

<script lang="ts">
    import {Component, Vue, Watch} from 'vue-property-decorator'
    import _allDemos from '../resources/live-search.json'
    import {matchesKeywords, splitKeywords} from './keywords-matching'
    import HighlightMatch from './components/HighlightMatch.vue'

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
                const url = this.demoPath(this.currentDemo)
                window.open(url)
            }
        }

        demoPath(demo: Demo): string {
            return '/demos/' + demo.name + '/index.html'
        }
    }
</script>

<style scoped>
    .live-search {
        text-align: center;
        padding: 20px;
    }

    .search-input input {
        width: 60%;
        height: 80px;
        font-size: 50px;
        border: 3px solid #22863a;
        color: #735c0f;
        padding: 10px;
    }

    .search-tips {
        font-size: 20px;
        padding: 10px;
    }

    .filtered-demos li {
        list-style: none;
        font-size: 24px;
        padding: 5px;
        text-align: left;
    }

    .selected-demo {
        background-color: #DDD;
    }
</style>
