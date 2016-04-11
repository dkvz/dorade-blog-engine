package models;

import java.util.*;

public class ArticleSummary {
	
	/**
	 * This is an imaginary ID, ordered from 1 to X
	 * to serve as an abstract ordering mechanism for the articles.
	 */
	private long id = -1;
	private String title;
	private String thumbImage;
	private List<ArticleTag> tags;
	private Date date;
	private String summary;
	private String author;
	private int commentsCount = 0;
	
	public ArticleSummary() {
		this.tags = new ArrayList<ArticleTag>();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getThumbImage() {
		return thumbImage;
	}

	public void setThumbImage(String thumbImage) {
		this.thumbImage = thumbImage;
	}

	public List<ArticleTag> getTags() {
		return tags;
	}

	public void setTags(List<ArticleTag> tags) {
		this.tags = tags;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public int getCommentsCount() {
		return commentsCount;
	}

	public void setCommentsCount(int commentsCount) {
		this.commentsCount = commentsCount;
	}
	
}
