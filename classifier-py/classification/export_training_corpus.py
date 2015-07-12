import sys
import os
import dbconfig
import mysql.connector


def query(limit, classes):
    return """
        SELECT tweet_cleaned
        FROM tweets_all
        WHERE classified IN ({classes})
        ORDER BY RAND()
        LIMIT {limit}
    """.format(
        limit=limit,
        classes="'{all}'".format(all="','".join(classes)),
    )


version = sys.argv[1]
limit = int(sys.argv[2])
home = os.path.expanduser("~")

path = os.path.join(
    home,
    'nltk_data{s}corpora{s}tweets_publish_choice_{version}'.format(
        version=version,
        s=os.sep,
    )
)

pos_dir = os.path.join(path, 'pos')
neg_dir = os.path.join(path, 'neg')

os.makedirs(pos_dir)
os.makedirs(neg_dir)

cnx = mysql.connector.connect(
    user=dbconfig.MYSQL_USER,
    password=dbconfig.MYSQL_PASSWORD,
    database=dbconfig.MYSQL_DATABASE,
    host=dbconfig.MYSQL_SERVER,
    port=dbconfig.MYSQL_PORT,
)

# positive examples
cursor = cnx.cursor()
cursor.execute(query(limit, ['interested', 'published']))
counter = 0
for tweet_text in cursor:
    with open(os.path.join(pos_dir, '{0}.txt'.format(counter)), 'w') as f:
        f.write(tweet_text)
print("Exported {c} positive examples to {out}".format(
    c=counter,
    out=pos_dir,
))

# negative examples
cursor = cnx.cursor()
cursor.execute(query(limit, ['ignored']))
counter = 0
for tweet_text in cursor:
    with open(os.path.join(neg_dir, '{0}.txt'.format(counter)), 'w') as f:
        f.write(tweet_text)
print("Exported {c} negative examples to {out}".format(
    c=counter,
    out=neg_dir,
))
