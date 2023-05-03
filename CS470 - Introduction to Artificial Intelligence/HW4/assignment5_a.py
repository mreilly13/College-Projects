# Attributions:
# - provided materials (files, slides, etc)
# - Keras documentation

import pickle
import numpy as np
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dense
from sklearn.preprocessing import MinMaxScaler

# parsing input data into normalized training and testing sets
with open('dev_data.pickle', 'rb') as f:
    (x_train, y_train), (x_test, y_test) = pickle.load(f)
x = x_train
y_scale = MinMaxScaler()
y = y_scale.fit_transform(y_train)
xt = x_test
yt_scale = MinMaxScaler()
yt = yt_scale.fit_transform(y_test)

# set up and train neural network
ep = 50
bs = 32
af = "linear"
model = Sequential()
model.add(Dense(y.shape[1], activation=af, name="Output"))
model.compile(loss='mse', optimizer='adam', metrics=['mse'])
model.fit(x, y, batch_size=bs, epochs=ep, verbose=2, validation_data=(xt, yt))
model.summary()
final_train_loss, final_train_accuracy = model.evaluate(x, y, verbose = 0)
final_test_loss, final_test_accuracy = model.evaluate(xt, yt, verbose = 0)
 
# test neural network and validate results
predictions = model.predict(xt)
results = yt_scale.inverse_transform(predictions)
difference = np.zeros((len(results), len(results[0])))
for i in range(len(results)):
    results[i][0] = max(round(results[i][0]), 0)
    results[i][1] = max(round(results[i][1]), 0)
    for j in range(results.shape[1]):
        difference[i][j] = results[i][j] - y_test[i][j]
print("Epochs:    ", ep,
      "\nBatch Size:", bs,
      "\nActivation:", af,
      "\nTrain MSE: ", round(final_train_loss, 4),
      "\nTest MSE:  ", round(final_test_loss, 4),
      "\nStandard Deviation:",
      "\n- complete:  ", round(np.std(difference[:,0]), 3), 
      "\n- incomplete:", round(np.std(difference[:,1]), 3),
      "\n- commission:", round(np.std(difference[:,2]), 2),
      "\n- revenue:   ", round(np.std(difference[:,3]), 2))