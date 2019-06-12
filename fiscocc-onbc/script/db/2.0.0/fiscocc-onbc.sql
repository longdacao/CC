CREATE TABLE t_evidence_status_info (
  evidence_id bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '证据ID',
  app_id varchar(32) NOT NULL COMMENT '机构识别ID',
  user_info varchar(256) NULL COMMENT '客户信息',
  customer_type varchar(32) NULL COMMENT '客户类型（0-个人；1-企业）',
  user_name varchar(64) NULL COMMENT '客户姓名',
  identification_type varchar(32) NOT NULL COMMENT '证件类型（0:身份证 1:护照 2:组织机构代码证）',
  identification_no varchar(64) NOT NULL COMMENT '证件号码',
  evidence_hash varchar(128) NOT NULL COMMENT '证据hash',
  ex_data varchar(1024) NULL COMMENT '扩展字段',
  set_side int(11) DEFAULT '0' COMMENT '存证发起方（0-当前机构；1-其他机构）',
  sign_or_not int(11) DEFAULT '0' COMMENT '发起方为其他机构时是否需要当期机构签名（0-需要；1-不需要）',
  sign_data varchar(512) COMMENT '签名数据',
  evidence_address varchar(128) DEFAULT '' COMMENT '证据链上地址',
  sign_flag int(11) NULL DEFAULT 0 COMMENT '签名状态（0-未签名；1-已签名）',
  notify_count int(11) DEFAULT 0 COMMENT '通知次数',
  chain_count int(11) DEFAULT 0 COMMENT '请求上链次数',
  version tinyint(4) DEFAULT 1 COMMENT '版本号:乐观锁',
  gmt_create datetime DEFAULT NULL COMMENT '创建时间',
  gmt_modify datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (evidence_id)
) COMMENT='证据状态信息表' ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE t_evidence_status_info ADD INDEX idx_tesi_escg_01 (evidence_address,app_id,sign_flag,chain_count,gmt_modify);
ALTER TABLE t_evidence_status_info ADD INDEX idx_tesi_esng_02 (evidence_address,app_id,sign_flag,notify_count,gmt_modify);

CREATE TABLE t_white_ip (
  id int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  ip varchar(32) NOT NULL COMMENT 'ip',
  remark varchar(256) NULL COMMENT '备注',
  create_dt datetime DEFAULT NULL COMMENT '创建时间',
  update_dt datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (id)
) COMMENT='ip白名单' ENGINE=InnoDB DEFAULT CHARSET=utf8;
ALTER TABLE t_white_ip ADD INDEX idx_wi_i (ip);