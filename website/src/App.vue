<template>
    <div>
        <div>Demo Driven Learning</div>
        <input type="text" v-model="keyword" autofocus/>
        <div>Search in {{totalDemoCount}} complete small demos</div>
        <hr/>
        <ul>
            <li v-for="demo in demos" v-bind:key="demo.name">
                <span>{{ demo.name }}</span>
                <span>{{ demo.description }}</span>
            </li>
        </ul>
    </div>
</template>

<script lang="ts">
    import {Component, Vue} from 'vue-property-decorator'
    import allDemos from '../resources/live-search.json'
    import {matchesKeywords, splitKeywords} from './keywords-matching'

    @Component({})
    export default class App extends Vue {
        private keyword: string = ''
        totalDemoCount: number = allDemos.length

        get demos() {
            const keywords = splitKeywords(this.keyword)
            console.log(keywords)
            return keywords.length === 0
                ? []
                : allDemos.filter(demo => matchesKeywords(demo.name, keywords))
        }
    }
</script>
