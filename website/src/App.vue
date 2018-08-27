<template>
    <div>
        <div>Demo Driven Learning</div>
        <input type="text" v-model="keyword" autofocus/>
        <div>Search in {{totalDemoCount}} complete small demos</div>
        <hr/>
        <ul>
            <li v-for="demo in demos" v-bind:key="demo.name">
                <HighlightMatch v-bind:text="demo.name" v-bind:keywords="keywords"/>
                <span>{{ demo.description }}</span>
            </li>
        </ul>
    </div>
</template>

<script lang="ts">
    import {Component, Vue} from 'vue-property-decorator'
    import allDemos from '../resources/live-search.json'
    import {matchesKeywords, splitKeywords} from './keywords-matching'
    import HighlightMatch from './components/HighlightMatch'

    @Component({
        components: {
            HighlightMatch
        }
    })
    export default class App extends Vue {
        private keyword: string = ''
        totalDemoCount: number = allDemos.length

        get keywords(): string[] {
            return splitKeywords(this.keyword)
        }

        get demos() {
            const keywords = this.keywords
            return keywords.length === 0
                ? []
                : allDemos.filter(demo => matchesKeywords(demo.name, keywords))
        }
    }
</script>
