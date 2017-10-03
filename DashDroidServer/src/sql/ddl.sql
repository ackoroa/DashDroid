CREATE TABLE `mpd` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `video_id` int(11) DEFAULT NULL,
  `file_path` varchar(255) DEFAULT NULL,
  `is_valid` char(1) DEFAULT NULL,
  `creation_datetime` datetime DEFAULT NULL,
  `last_modified_datetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_video_mpd_idx` (`video_id`),
  CONSTRAINT `fk_video_mpd` FOREIGN KEY (`video_id`) REFERENCES `video` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8

CREATE TABLE `segment` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `video_id` int(11) DEFAULT NULL,
  `file_path` varchar(255) DEFAULT NULL,
  `segment_type` char(1) DEFAULT NULL,
  `creation_datetime` datetime DEFAULT NULL,
  `last_modified_datetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `video_id_idx` (`video_id`),
  CONSTRAINT `fk_segment_video` FOREIGN KEY (`video_id`) REFERENCES `video` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8

CREATE TABLE `video` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `is_full_video` char(1) DEFAULT NULL,
  `creation_datetime` datetime DEFAULT NULL,
  `last_modified_datetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8