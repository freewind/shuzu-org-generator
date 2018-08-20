<template>
    <div>
        <div>Demo Driven Learning</div>
        <input type="text" v-model="keyword" autofocus/>
        <div>Search in {{demoCount}} complete small demos</div>
        <hr/>
        <ul>
            <li v-for="demo in demos">{{ demo }}</li>
        </ul>
    </div>
</template>

<script lang="ts">
    import {Component, Prop, Vue} from 'vue-property-decorator'

    const initDemos = ['Java', 'JavaScript', 'Kotlin', 'Angular', 'Scala']

    @Component({})
    export default class App extends Vue {
        @Prop() private keyword: string = ''
        demoCount: number = 0

        get demos() {
            return initDemos.filter(demo =>
                demo.toLocaleLowerCase().includes(this.keyword.trim().toLowerCase()),
            )
        }
    }
</script>
