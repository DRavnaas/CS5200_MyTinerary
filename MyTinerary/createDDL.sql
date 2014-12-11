CREATE TABLE AuthenticatedUser (id VARCHAR(255) NOT NULL, authId VARCHAR(255) NOT NULL, authToken VARCHAR(255) NOT NULL, firstName VARCHAR(50) NULL, lastAuthCheck DATETIME NOT NULL, lastName VARCHAR(50) NULL, PRIMARY KEY (id))
CREATE TABLE TripBase (id INTEGER IDENTITY NOT NULL, TRIP_TYPE VARCHAR(20) NULL, destination VARCHAR(255) NOT NULL, endDate DATETIME NULL, friendlyName VARCHAR(255) NOT NULL, startDate DATETIME NOT NULL, PRIMARY KEY (id))
CREATE TABLE ShareableTrip (id INTEGER NOT NULL, tripOwner VARCHAR(255) NOT NULL, PRIMARY KEY (id))
CREATE TABLE SharedTrip (id INTEGER IDENTITY NOT NULL, sharedUser VARCHAR(255) NOT NULL, trip INTEGER NOT NULL, PRIMARY KEY (id))
CREATE TABLE SubTrip (id INTEGER NOT NULL, SUBTRIPDETAIL_id INTEGER NULL, PRIMARY KEY (id))
CREATE TABLE TripDetail (id INTEGER IDENTITY NOT NULL, DETAIL_TYPE VARCHAR(20) NULL, friendlyName VARCHAR(255) NOT NULL, tripId INTEGER NOT NULL, PRIMARY KEY (id))
CREATE TABLE ToDoListDetail (id INTEGER NOT NULL, PRIMARY KEY (id))
CREATE TABLE ToDoListItem (id INTEGER IDENTITY NOT NULL, completed BIT default 0 NOT NULL, itemDescription VARCHAR(255) NOT NULL, listId INTEGER NOT NULL, PRIMARY KEY (id))
CREATE TABLE ReservationDetail (id INTEGER NOT NULL, duration VARCHAR(50) NULL, notes VARCHAR(255) NULL, start VARCHAR(50) NULL, PRIMARY KEY (id))
CREATE TABLE SubtripDetail (id INTEGER NOT NULL, PRIMARY KEY (id))
CREATE TABLE FlightDetail (id INTEGER NOT NULL, confirmationCode VARCHAR(255) NULL, flightSvcDetailId VARCHAR(255) NOT NULL, PRIMARY KEY (id))
CREATE TABLE ActivityLinkDetail (id INTEGER NOT NULL, url VARCHAR(2048) NOT NULL, enhancedDetailId VARCHAR(255) NULL, PRIMARY KEY (id))
CREATE TABLE EnhancedLinkDetail (providerUrlKey VARCHAR(255) NOT NULL, location VARCHAR(255) NULL, name VARCHAR(255) NOT NULL, thumbnailUrl VARCHAR(255) NULL, PRIMARY KEY (providerUrlKey))
CREATE TABLE FlightServiceDetail (id VARCHAR(255) NOT NULL, airlineCode VARCHAR(5) NOT NULL, arrivalCity VARCHAR(255) NOT NULL, arrivalTime DATETIME NOT NULL, departureCity VARCHAR(255) NOT NULL, departureTime DATETIME NOT NULL, flightNumber INTEGER NOT NULL, lastRefreshed DATETIME NULL, PRIMARY KEY (id))
CREATE TABLE TripNotification (id INTEGER IDENTITY NOT NULL, addedDate DATETIME NULL, text VARCHAR(255) NOT NULL, listId INTEGER NOT NULL, PRIMARY KEY (id))
CREATE TABLE TripNotificationList (id INTEGER IDENTITY NOT NULL, containingTrip INTEGER NULL, PRIMARY KEY (id))
ALTER TABLE TripBase ADD CONSTRAINT UNQ_TripBase_0 UNIQUE (friendlyName)
ALTER TABLE SharedTrip ADD CONSTRAINT UNQ_SharedTrip_0 UNIQUE (trip, sharedUser)
ALTER TABLE TripDetail ADD CONSTRAINT UNQ_TripDetail_0 UNIQUE (friendlyName, DETAIL_TYPE, tripId)
ALTER TABLE FlightServiceDetail ADD CONSTRAINT FlightServiceDetail0 UNIQUE (airlineCode, flightNumber, departureCity, departureTime, arrivalCity)
ALTER TABLE TripNotificationList ADD CONSTRAINT TripNotificationList0 UNIQUE (containingTrip)
ALTER TABLE ShareableTrip ADD CONSTRAINT FK_ShareableTrip_id FOREIGN KEY (id) REFERENCES TripBase (id)
ALTER TABLE ShareableTrip ADD CONSTRAINT ShareableTriptripOwner FOREIGN KEY (tripOwner) REFERENCES AuthenticatedUser (id) ON DELETE CASCADE
ALTER TABLE SharedTrip ADD CONSTRAINT SharedTrip_sharedUser FOREIGN KEY (sharedUser) REFERENCES AuthenticatedUser (id) ON DELETE CASCADE
ALTER TABLE SharedTrip ADD CONSTRAINT FK_SharedTrip_trip FOREIGN KEY (trip) REFERENCES TripBase (id) ON DELETE CASCADE
ALTER TABLE SubTrip ADD CONSTRAINT FK_SubTrip_id FOREIGN KEY (id) REFERENCES TripBase (id)
ALTER TABLE SubTrip ADD CONSTRAINT SubTripSUBTRIPDETAILid FOREIGN KEY (SUBTRIPDETAIL_id) REFERENCES TripDetail (id)
ALTER TABLE TripDetail ADD CONSTRAINT FK_TripDetail_tripId FOREIGN KEY (tripId) REFERENCES TripBase (id) ON DELETE CASCADE
ALTER TABLE ToDoListDetail ADD CONSTRAINT FK_ToDoListDetail_id FOREIGN KEY (id) REFERENCES TripDetail (id)
ALTER TABLE ToDoListItem ADD CONSTRAINT FK_ToDoListItem_listId FOREIGN KEY (listId) REFERENCES TripDetail (id) ON DELETE CASCADE
ALTER TABLE ReservationDetail ADD CONSTRAINT ReservationDetail_id FOREIGN KEY (id) REFERENCES TripDetail (id)
ALTER TABLE SubtripDetail ADD CONSTRAINT FK_SubtripDetail_id FOREIGN KEY (id) REFERENCES TripDetail (id)
ALTER TABLE FlightDetail ADD CONSTRAINT FlghtDtlflghtSvcDtilId FOREIGN KEY (flightSvcDetailId) REFERENCES FlightServiceDetail (id)
ALTER TABLE FlightDetail ADD CONSTRAINT FK_FlightDetail_id FOREIGN KEY (id) REFERENCES TripDetail (id)
ALTER TABLE ActivityLinkDetail ADD CONSTRAINT ctvtyLnkDtailnhncdDtld FOREIGN KEY (enhancedDetailId) REFERENCES EnhancedLinkDetail (providerUrlKey)
ALTER TABLE ActivityLinkDetail ADD CONSTRAINT ActivityLinkDetail_id FOREIGN KEY (id) REFERENCES TripDetail (id)
ALTER TABLE TripNotification ADD CONSTRAINT TripNotificationlistId FOREIGN KEY (listId) REFERENCES TripNotificationList (id)
ALTER TABLE TripNotificationList ADD CONSTRAINT TrpNtfctnListcntnngTrp FOREIGN KEY (containingTrip) REFERENCES TripBase (id)