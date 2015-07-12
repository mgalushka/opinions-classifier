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

try:
    os.makedirs(pos_dir)
    os.makedirs(neg_dir)
except Exception, e:
    print("Already existed")

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
counter = 1
for tweet_text in cursor:
    path = os.path.join(pos_dir, '{0}.txt'.format(counter))
    print("Exporting positive sample: {0}".format(path))
    with open(path, 'w') as f:
        f.write(tweet_text)
    counter += 1
print("Exported {c} positive examples to {out}".format(
    c=counter,
    out=pos_dir,
))

# negative examples
cursor = cnx.cursor()
cursor.execute(query(limit, ['ignored']))
counter = 1
for tweet_text in cursor:
    path = os.path.join(neg_dir, '{0}.txt'.format(counter))
    print("Exporting negative sample: {0}".format(path))
    with open(path, 'w') as f:
        f.write(tweet_text)
    counter += 1
print("Exported {c} negative examples to {out}".format(
    c=counter,
    out=neg_dir,
))
