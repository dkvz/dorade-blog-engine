# Dorade Blog Backend

Play Framework App. made in a shamefully short time.

## Schema modification
I added short articles to what I serve.

I intend to basically put it in the same table as "normal" articles.

It's just that there's is going to be a second endpoint to get those shorts.

```
ALTER TABLE articles ADD `short` INTEGER NOT NULL DEFAULT 0;
```

## TODO
* I have to say it bothers me a lot that this project uses tabs and not space. I know right?
* Add some sort of injection mechanism for articles (and shorts). Maybe not an API call, maybe something local-filesystem related.
* We're ordering by ids and not date, could be a problem one day. Although the authoring tool has a way to assign new ids to work around this. Also using id to order is the fastest thing you can do. So there's that.