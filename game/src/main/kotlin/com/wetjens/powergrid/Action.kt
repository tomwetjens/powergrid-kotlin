package com.wetjens.powergrid

interface Action {

    fun apply(powerGrid: PowerGrid): PowerGrid

}