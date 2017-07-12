package com.wetjens.powerline

interface Action {

    fun apply(powerGrid: PowerGrid): PowerGrid

}