import sys
import os
import dbconfig
import mysql.connector


def min_query(account_id):
    return """
    SELECT
        LEAST(
            SUM(if(classified IN ('interested', 'published'), 1, 0)),
            SUM(if(classified IN ('ignored'), 1, 0))
        ) as max_max
    FROM tweets_all
    WHERE
       account_id = {account_id} AND
       classified IS NOT NULL AND
       tweet_cleaned IS NOT NULL
    """.format(
        account_id=account_id,
    )


def query(account_id, limit, classes):
    return """
        SELECT tweet_cleaned
        FROM tweets_all
        WHERE
            account_id = {account_id} AND
            classified IN ({classes}) AND
            tweet_cleaned IS NOT NULL
        ORDER BY RAND()
        LIMIT {limit}
    """.format(
        account_id=account_id,
        limit=limit,
        classes="'{all}'".format(all="','".join(classes)),
    )


account_id = sys.argv[1]
version = sys.argv[2]

home = os.path.expanduser("~")
path = os.path.join(
    home,
    'nltk_data{s}corpora{s}tweets_publish_choice_{account}{s}{version}'.format(
        account=account_id,
        version=version,
        s=os.sep,
    )
)

pos_dir = os.path.join(path, 'pos')
neg_dir = os.path.join(path, 'neg')

if not os.path.exists(pos_dir):
    os.makedirs(pos_dir)
if not os.path.exists(neg_dir):
    os.makedirs(neg_dir)

cnx = mysql.connector.connect(
    user=dbconfig.MYSQL_USER,
    password=dbconfig.MYSQL_PASSWORD,
    database=dbconfig.MYSQL_DATABASE,
    host=dbconfig.MYSQL_SERVER,
    port=dbconfig.MYSQL_PORT,
)

# number of samples
cursor = cnx.cursor()
cursor.execute(min_query(account_id))
limit = 0
for (min_value,) in cursor:
    limit = int(min_value)

print('Exporting {0} samples'.format(limit))

# positive examples
cursor = cnx.cursor()
cursor.execute(query(account_id, limit, ['interested', 'published']))
counter = 1
for (tweet_text,) in cursor:
    path = os.path.join(pos_dir, '{0}.txt'.format(counter))
    print("Exporting positive sample: {0}".format(path))
    with open(path, 'w') as f:
        f.write(tweet_text.encode('utf-8', 'ignore'))
    counter += 1
print("Exported {c} positive examples to {out}".format(
    c=counter - 1,
    out=pos_dir,
))

# negative examples
cursor = cnx.cursor()
cursor.execute(query(account_id, limit, ['ignored']))
counter = 1
for (tweet_text,) in cursor:
    path = os.path.join(neg_dir, '{0}.txt'.format(counter))
    print("Exporting negative sample: {0}".format(path))
    with open(path, 'w') as f:
        f.write(tweet_text.encode('utf-8', 'ignore'))
    counter += 1
print("Exported {c} negative examples to {out}".format(
    c=counter - 1,
    out=neg_dir,
))
