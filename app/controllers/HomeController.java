package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Profile;
import models.User;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by lubuntu on 8/21/16.
 */
public class HomeController extends Controller {

    @Inject
    ObjectMapper objectMapper;

    @Inject
    FormFactory formFactory;

    public Result getProfile(Long userId){

        User user = User.find.byId(userId);
        Profile profile = Profile.find.byId(user.profile.id);
        ObjectNode data = objectMapper.createObjectNode();

        List<Long> connectedUserIds=user.connections.stream().map(x -> x.id).collect(Collectors.toList());

        List<Long> connectionRequestSentUserIds = user.connectionRequestsSent.stream().map(x -> x.receiver.id).collect(Collectors.toList());

        List<JsonNode> suggestions = User.find.all().stream().filter(x -> !connectedUserIds.contains(x.id) &&
                !connectionRequestSentUserIds.contains(x.id) && !Objects.equals(x.id, userId))
                .map(x -> {
                    ObjectNode userJson = objectMapper.createObjectNode();
                    userJson.put("email",x.email);
                    userJson.put("id",x.id);
                    return userJson;
                })
                .collect(Collectors.toList());

        data.set("suggestion",objectMapper.valueToTree(suggestions));

        data.set("connections",objectMapper.valueToTree( user.connections.stream()
             .map(x -> {
                 User connectedUser = User.find.byId(x.id);
                 Profile connectedProfile = Profile.find.byId(connectedUser.profile.id);
                 ObjectNode connectionjson = objectMapper.createObjectNode();
                 connectionjson.put("email",connectedUser.email);
                 connectionjson.put("firstName",connectedProfile.firstName);
                 connectionjson.put("lastName",connectedProfile.lastName);
                 return connectionjson;
             }).collect(Collectors.toList())));

        data.set("connectionRequestsReceived",objectMapper.valueToTree( user.connectionRequestsReceived.stream()
                .map(x -> {
                    User requestor = User.find.byId(x.sender.id);
                    Profile requestorProfile = Profile.find.byId(requestor.profile.id);
                    ObjectNode requestorjson = objectMapper.createObjectNode();
                    requestorjson.put("email",requestor.email);
                    requestorjson.put("firstName",requestorProfile.firstName);
                    requestorjson.put("lastName",requestorProfile.lastName);
                    requestorjson.put("connectionRequestId",x.id);
                    return requestorjson;
                }).collect(Collectors.toList())));

        return ok(data);

    }
}
