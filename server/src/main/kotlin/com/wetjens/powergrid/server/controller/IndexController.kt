package com.wetjens.powergrid.server.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class IndexController {

    @RequestMapping("/")
    fun getIndex(): String {
        return "index"
    }

}