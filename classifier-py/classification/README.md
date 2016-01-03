## Start service

Default port: 8077

`python svm_service.py PORT TRAINING_MODEL_VERSION`

Example:
`python svm_service.py 8077 0.5`

## Export new model version

Model is based on positive/negative samples (same number of both types) and SVM classifier used to distinguish
tweets which are helpful from those which are not helpful - training data based on user input.

`python export_training_corpus.py ACCOUNT_ID VERSION_NUMBER`

Example:
`python export_training_corpus.py 1 0.6`