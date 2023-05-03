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

# set up, train, and test the neural network
ep = 50
bs = 32
afh = "relu"
afo = "linear"
model = Sequential()
model.add(Dense(1000, activation=afh, name="Hidden1"))
model.add(Dense(y.shape[1], activation=afo, name="Output"))
model.compile(loss='mse', optimizer="adam", metrics=['mse'])
model.fit(x, y, batch_size=bs, epochs=ep, verbose=1, validation_data=(xt, yt))
model.summary()
print("\nFinal evaluation of model:")
predictions = model.predict(xt)
final_train_loss, final_train_accuracy = model.evaluate(x, y)
final_test_loss, final_test_accuracy = model.evaluate(xt, yt)
 
# validate results
results = yt_scale.inverse_transform(predictions)
difference = np.zeros((len(results), len(results[0])))
for i in range(len(results)):
    results[i][0] = max(round(results[i][0]), 0)
    results[i][1] = max(round(results[i][1]), 0)
    for j in range(results.shape[1]):
        difference[i][j] = results[i][j] - y_test[i][j]

print("\nEpochs:    ", ep,
      "\nBatch Size:", bs,
      "\nHidden AF: ", afh,
      "\nOutput AF: ", afo,
      "\nTrain MSE: ", round(final_train_loss, 4),
      "\nTest MSE:  ", round(final_test_loss, 4),
      "\nStandard Deviation:",
      "\n- complete:  ", round(np.std(difference[:,0]), 3), 
      "\n- incomplete:", round(np.std(difference[:,1]), 3),
      "\n- commission:", round(np.std(difference[:,2]), 2),
      "\n- revenue:   ", round(np.std(difference[:,3]), 2))

"""
The network structure that I decided on was to simply add a hidden layer to the
model from part (a). This hidden layer uses the 'relu' activation function and
has an output dimension of 1000. I decided on this number because I was getting 
better results the larger the output dimension was, but this effect was 
exponential; the improvements became smaller as the value grew. I started with
10, changed it to 100 and saw significant improvement, then changed it to 1000
and saw marked improvement with a noticeable decrease in performance. Starting
at 10,000, the improvements became minimal, while the decrease in performance
became enormous, so I left it at 1000. This is also the process I used to decide
how many epochs to use; I saw diminishing returns as the number of epochs grew,
with anything over 50 giving negligible improvement. I chose a batch size of 32
simply because it is the default value, only storing it separately to enable
ease of printing.

My changes significantly improve the quality of the predictions, as measured by 
the standard deviation of the differences between the prediction values and the 
actual values for each field in the test set. The standard deviation of the 
difference between predictions in part (a) for the complete and incomplete 
fields was around 1; in (b), these standard deviations are around .5, half the 
value from (a). For the comission, the standard deviation in (a) was around 220,
and in (b) this dropped to under 50, less than a fourth. For the revenue, the 
standard deviation in (a) was around 25,000, and in (b) it was under 9,000, 
about a third the value from (a). Across all metrics, this hidden layer markedly
reduced the standard deviation of the difference between predictions and actual
values, which indicates more predictive power. 

Despite the simplicity of the change I settled on, I performed extensive tests,
none of which led to any substantial improvement over my final approach. 
I tried:
- adding additional hidden layers
- varying the output dimensions of the hidden layers
- changing the activation functions of the hidden layers
- changing the optimizer function
- changing the learning rate
- changing the loss function

I tried dozens of combinations of changes, and none of them had results that 
were better than adding a single 'relu' hidden layer with an output size of
1000. As this is a non-temporal regression problem, I did not think a recurrent 
layer made sense, so I only tried variations of Dense layers.
"""