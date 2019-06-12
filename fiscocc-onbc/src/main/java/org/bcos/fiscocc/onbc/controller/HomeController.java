package org.bcos.fiscocc.onbc.controller;

import org.bcos.fiscocc.onbc.dto.PageData;
import org.bcos.fiscocc.onbc.util.Constants;
import org.bcos.fiscocc.onbc.util.ResultUtil;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@RequestMapping(value = "fiscocc-onbc/home")
public class HomeController {
	

	@RequestMapping("permissionDenied")
	@ResponseBody
	public PageData permissionDenied() {
		return ResultUtil.error(Constants.PERMISSION_DENIED.getErrorMsg(), Constants.PERMISSION_DENIED.getErrorCode());
	}

}
