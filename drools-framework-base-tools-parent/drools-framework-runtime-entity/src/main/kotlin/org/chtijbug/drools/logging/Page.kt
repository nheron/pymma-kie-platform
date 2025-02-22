package org.chtijbug.drools.logging

class Page {
    var currentIndex: Int? = null
    var totalCount: Long? = null

    /**
     * Setting a default value tha we can override
     */
    var maxItemPerPage = 5

    fun Page() {
        /** nop  */
    }

    fun Page(currentIndex: Int?, totalCount: Long?, maxItemPerPage: Int) {
        this.currentIndex = currentIndex
        this.totalCount = totalCount
        this.maxItemPerPage = maxItemPerPage
    }
}