// Backfill reply_comment -> comment.replies
// Run once in mongosh after app code deploy.

const cursor = db.reply_comment.find();
let migrated = 0;
let skipped = 0;

cursor.forEach((reply) => {
  const replyId = reply._id;
  const commentId = reply.comment_id;

  const comment = db.comment.findOne(
    { _id: commentId },
    { projection: { _id: 1, replies: 1 } },
  );

  if (!comment) {
    skipped += 1;
    return;
  }

  const alreadyExists = Array.isArray(comment.replies)
    && comment.replies.some((item) => item._id === replyId);

  if (alreadyExists) {
    skipped += 1;
    return;
  }

  db.comment.updateOne(
    { _id: commentId },
    {
      $push: {
        replies: {
          _id: replyId,
          comment_id: commentId,
          userId: reply.userId,
          content: reply.content,
          likes: reply.likes || [],
          created_datetime: reply.created_datetime,
          created_at: reply.created_at,
          created_by: reply.created_by || null,
          updated_at: reply.updated_at || null,
          updated_by: reply.updated_by || null,
        },
      },
      $set: {
        reply: true,
      },
    },
  );

  migrated += 1;
});

printjson({ migrated, skipped });

// Verify counts before dropping collection:
// db.reply_comment.countDocuments()
// db.comment.aggregate([
//   { $project: { count: { $size: { $ifNull: ["$replies", []] } } } },
//   { $group: { _id: null, total: { $sum: "$count" } } },
// ])
