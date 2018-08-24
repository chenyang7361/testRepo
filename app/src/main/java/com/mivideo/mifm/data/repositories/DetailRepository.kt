package com.mivideo.mifm.data.repositories

import com.mivideo.mifm.network.service.DetailService

class DetailRepository(service: DetailService) : DetailService by service
