<template>
    <div>
        <div>Demo Driven Learning</div>
        <input type="text" v-model="keyword" autofocus/>
        <div>Search in {{demoCount}} complete small demos</div>
        <hr/>
        <ul>
            <li v-for="demo in demos">{{ demo.name }}</li>
        </ul>
    </div>
</template>

<script lang="ts">
    import {Component, Prop, Vue} from 'vue-property-decorator'
    import allDemos from '../resources/live-search.json'

    @Component({})
    export default class App extends Vue {
        @Prop() private keyword: string = ''
        demoCount: number = 0

        get demos() {
            return allDemos.filter(demo =>
                demo.name.toLocaleLowerCase().includes(this.keyword.trim().toLowerCase()),
            )
        }
    }
</script>
