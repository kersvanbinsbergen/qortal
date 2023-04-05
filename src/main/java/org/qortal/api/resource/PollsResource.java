package org.qortal.api.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
//import java.util.function.Predicate;
//import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
//import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.qortal.api.ApiError;
import org.qortal.api.ApiErrors;
import org.qortal.api.ApiException;
import org.qortal.api.ApiExceptionFactory;
//import org.qortal.crypto.Crypto;
//import org.qortal.data.transaction.CreatePollTransactionData;
//import org.qortal.data.transaction.VoteOnPollTransactionData;
import org.qortal.data.voting.PollData;
//import org.qortal.data.voting.PollOptionData;
//import org.qortal.data.voting.VoteOnPollData;
import org.qortal.repository.DataException;
import org.qortal.repository.Repository;
import org.qortal.repository.RepositoryManager;
//import org.qortal.settings.Settings;
//import org.qortal.transaction.Transaction;
//import org.qortal.transaction.Transaction.ValidationResult;
//import org.qortal.transform.TransformationException;
//import org.qortal.transform.transaction.CreatePollTransactionTransformer;
//import org.qortal.transform.transaction.VoteOnPollTransactionTransformer;
//import org.qortal.utils.Base58;

@Path("/polls")
@Tag(name = "Polls")
public class PollsResource {

	@Context
	HttpServletRequest request;

	@GET
	@Operation(
		summary = "List all polls",
		responses = {
			@ApiResponse(
				description = "poll info",
				content = @Content(
					mediaType = MediaType.APPLICATION_JSON,
					array = @ArraySchema(schema = @Schema(implementation = PollData.class))
				)
			)
		}
	)
	@ApiErrors({ApiError.REPOSITORY_ISSUE})
	public List<PollData> getAllPolls(@Parameter(
		ref = "limit"
	) @QueryParam("limit") Integer limit, @Parameter(
		ref = "offset"
	) @QueryParam("offset") Integer offset, @Parameter(
		ref = "reverse"
	) @QueryParam("reverse") Boolean reverse) {
		try (final Repository repository = RepositoryManager.getRepository()) {
			List<PollData> allPollData = repository.getVotingRepository().getAllPolls(limit, offset, reverse);
			return allPollData;
		} catch (DataException e) {
			throw ApiExceptionFactory.INSTANCE.createException(request, ApiError.REPOSITORY_ISSUE, e);
		}
	}

    @GET
	@Path("/{pollName}")
	@Operation(
		summary = "Info on poll",
		responses = {
			@ApiResponse(
				description = "poll info",
				content = @Content(
					mediaType = MediaType.APPLICATION_JSON,
					schema = @Schema(implementation = PollData.class)
				)
			)
		}
	)
	@ApiErrors({ApiError.REPOSITORY_ISSUE})
	public PollData getPollData(@PathParam("pollName") String pollName) {
		try (final Repository repository = RepositoryManager.getRepository()) {
			PollData pollData = repository.getVotingRepository().fromPollName(pollName);
			if (pollData == null)
				throw ApiExceptionFactory.INSTANCE.createException(request, ApiError.POLL_NO_EXISTS);

			return pollData;
		} catch (ApiException e) {
			throw e;
		} catch (DataException e) {
			throw ApiExceptionFactory.INSTANCE.createException(request, ApiError.REPOSITORY_ISSUE, e);
		}
	}
}
