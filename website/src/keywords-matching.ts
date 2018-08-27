import _ from 'lodash'

function splitByFirst(text: string, keyword: string): undefined | [string, string] {
    const result = text.split(new RegExp(`${keyword}(.*)`))
    if (result === undefined || result.length !== 3) {
        return undefined
    } else {
        return [result[0], result[1]]
    }
}

function matchKeyword(strings: string[], keyword: string): boolean {
    const result = [...strings]
    for (const index in strings) {
        const str = strings[index]
        const split = splitByFirst(str, keyword)
        if (split !== undefined) {
            result.splice(parseInt(index), 1, ...split.filter(x => !_.isEmpty(x)))
            return true
        }
    }
    return false
}

export function splitKeywords(keyword: string): string[] {
    const keywords = _.uniq(keyword.trim().toLowerCase().split(/\s+/)).sort((a, b) => {
        if (a.length === b.length) {
            return a > b ? 1 : -1
        } else {
            return a.length - b.length
        }
    })
    return keywords.filter(x => !_.isEmpty(x))
}


export function matchesKeywords(text: string, keywords: string[]): boolean {
    const parts = [text.toLowerCase()]
    for (const keyword of keywords) {
        if (!matchKeyword(parts, keyword)) {
            return false
        }
    }
    return true
}