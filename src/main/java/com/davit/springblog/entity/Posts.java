package com.davit.springblog.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name ="posts")
@Data
public class Posts {

	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer postId;

	@Column(name ="post_title" ,length =100 ,nullable =false)
	private String title;

	private String content;

	@Column(name ="post_image")
	private String postImage;

	@Column(name = "created", updatable = false)
    private LocalDateTime created;
    
    @Column(name = "updated", insertable = false)
    private LocalDateTime updated;

	private boolean isDeleted = false;

	@ManyToOne
	private Categories categories;

	@ManyToOne
	private Users users;
}
