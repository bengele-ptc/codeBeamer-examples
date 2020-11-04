//Must be in com.intland.codebeamer.controller.rest.v2 package to show up in Swagger documentation
package com.intland.codebeamer.controller.rest.v2.examples;

import static com.intland.codebeamer.utils.MessageFormatterUtil.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intland.codebeamer.ajax.tree.TrackerHomePageTreeNode;
import com.intland.codebeamer.controller.rest.v2.AbstractRestController;
import com.intland.codebeamer.controller.rest.v2.exception.ResourceForbiddenException;
import com.intland.codebeamer.controller.rest.v2.exception.ResourceNotFoundException;
import com.intland.codebeamer.controller.rest.v2.exception.ResourceUnauthorizedException;
import com.intland.codebeamer.controller.support.TrackerHomePageSupport;
import com.intland.codebeamer.manager.AccessRightsException;
import com.intland.codebeamer.manager.TrackerManager;
import com.intland.codebeamer.persistence.dto.TrackerDto;
import com.intland.codebeamer.persistence.dto.UserDto;
import com.intland.codebeamer.utils.Common;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@OpenAPIDefinition
@Validated
@RestController
@RequestMapping(AbstractRestController.API_URI_V3)
public class CustomRest extends AbstractRestController {

	private final TrackerManager trackerManager;

	private final TrackerHomePageSupport homeSupport;

	public CustomRest(TrackerManager trackerManager, TrackerHomePageSupport homeSupport) {
		this.trackerManager = trackerManager;
		this.homeSupport = homeSupport;
	}

	@Operation(summary = "Move to Folder", tags = { "Tracker", "Custom" }, description = "Move Tracker into a folder.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404") })
	@RequestMapping(value = "trackers/{trackerId}/moveTo/{path}", method = RequestMethod.POST)
	public void getTrackers(@Parameter(hidden = true) HttpServletRequest request, @PathVariable("trackerId") Integer trackerId,
			@PathVariable("path") String path)
			throws ResourceNotFoundException, ResourceUnauthorizedException {
		String url = format("trackers/{trackerId}/moveTo/{path}", trackerId, path);

		UserDto user = authenticationSupport.getUser();
		TrackerDto tracker = trackerManager.findById(user, Collections.singletonList(trackerId)).stream().findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("Tracker is not found.", url));


		try {
			Integer projectId = tracker.getProject().getId();
			List<TrackerHomePageTreeNode> treeNodes = homeSupport.getTreeNodes(request, user, projectId, null, null, false);
			removeTrackerNode(treeNodes, trackerId);
			findOrCreateTargetFolder(path, treeNodes).getChildren().add(createTrackerNode(tracker));

			homeSupport.storeStructure(request, user, projectId, new ObjectMapper().writeValueAsString(mapNodes(treeNodes)));
		} catch (AccessRightsException e) {
			throw new ResourceUnauthorizedException(e.getMessage(), url);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}

	}

	private TrackerHomePageTreeNode createTrackerNode(TrackerDto tracker) {
		TrackerHomePageTreeNode trackerNode = new TrackerHomePageTreeNode(null, null);
		trackerNode.setTrackerId(tracker.getId());
		return trackerNode;
	}

	private TrackerHomePageTreeNode findOrCreateTargetFolder(String path, List<TrackerHomePageTreeNode> treeNodes) {
		return treeNodes.stream()
				.filter(node -> node.isFolder() && node.getText().equals(path))
				.findFirst().orElseGet(() -> {
					TrackerHomePageTreeNode newNode = new TrackerHomePageTreeNode(null, path);
					newNode.setFolder(true);
					return newNode;
				});
	}

	private void removeTrackerNode(List<TrackerHomePageTreeNode> treeNodes, Integer trackerId) {
		for(Iterator<TrackerHomePageTreeNode> iterator = treeNodes.iterator(); iterator.hasNext();) {
			TrackerHomePageTreeNode node = iterator.next();
			removeTrackerNode(node.getChildren(), trackerId);
			if(trackerId.equals(node.getTrackerId())) {
				iterator.remove();
			}
		}
	}

	private List<Map<String, Object>> mapNodes(List<TrackerHomePageTreeNode> nodes) {
		return nodes.stream().map(this::mapNode).collect(Collectors.toList());
	}

	private Map<String, Object> mapNode(TrackerHomePageTreeNode node) {
		Map<String, Object> attributes = new HashMap<>();
		if (node.isFolder()) {
			attributes.put("isFolder", Boolean.TRUE);
			attributes.put("text", node.getText());
		} else {
			attributes.put("trackerId", node.getId());
		}
		if (node.getChildren() != null) {
			attributes.put("children", mapNodes(node.getChildren()));
		}
		return attributes;
	}

}
