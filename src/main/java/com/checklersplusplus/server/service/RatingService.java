package com.checklersplusplus.server.service;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checklersplusplus.server.dao.GameRepository;
import com.checklersplusplus.server.dao.RatingRepository;
import com.checklersplusplus.server.entities.internal.Rating;
import com.checklersplusplus.server.exception.CheckersPlusPlusServerException;
import com.checklersplusplus.server.exception.GameNotFoundException;
import com.checklersplusplus.server.model.GameModel;
import com.checklersplusplus.server.model.RatingModel;

@Service
@Transactional
public class RatingService {

	private static final Logger logger = LoggerFactory.getLogger(RatingService.class);

	@Autowired
	private RatingRepository ratingRepository;
	
	@Autowired
	private GameRepository gameRepository;
	
	public Rating getRatingForPlayer(UUID accountId) throws CheckersPlusPlusServerException {
		Optional<RatingModel> rating = ratingRepository.findByAccountId(accountId);
		
		if (rating.isEmpty()) {
			throw new CheckersPlusPlusServerException("Illegal state. Missing rating");
		}
		
		return new Rating(rating.get().getRating(), accountId);
	}
	
	public void updatePlayerRatings(UUID gameId) throws CheckersPlusPlusServerException {
		Optional<GameModel> game = gameRepository.getByGameId(gameId);
		
		if (game.isEmpty()) {
			throw new GameNotFoundException();
		}
		
		if (game.get().getWinnerId() == null) {
			throw new CheckersPlusPlusServerException("Game not complete");
		}
		
		UUID winnerAccountId = game.get().getWinnerId();
		UUID loserAccountId = game.get().getWinnerId().equals(game.get().getBlackId()) ? game.get().getRedId() : game.get().getBlackId();
		Optional<RatingModel> winnerRating = ratingRepository.findByAccountId(winnerAccountId);
		Optional<RatingModel> loserRating = ratingRepository.findByAccountId(loserAccountId);
		
		if (winnerRating.isEmpty() || loserRating.isEmpty()) {
			if (winnerRating.isEmpty()) {
				logger.error("No rating found for accountId: " + winnerAccountId);
			}
			
			if (loserRating.isEmpty()) {
				logger.error("No rating found for accountId: " + loserAccountId);
			}
			
			throw new CheckersPlusPlusServerException("Illegal state. Missing rating");
		}
		
		Pair<Integer, Integer> winnerAndLoserUpdatedRatings = calculateNewPlayerRatings(winnerRating.get(), loserRating.get());
		winnerRating.get().setRating(winnerAndLoserUpdatedRatings.getFirst());
		loserRating.get().setRating(winnerAndLoserUpdatedRatings.getSecond());
		ratingRepository.save(winnerRating.get());
		ratingRepository.save(loserRating.get());
	}

	private Pair<Integer, Integer> calculateNewPlayerRatings(RatingModel winnerRating, RatingModel loserRating) {
		Integer newWinnerRating = null;
		Integer newLoserRating = null;
		Integer winnerInitialRating = winnerRating.getRating();
		Integer loserInitialRating = loserRating.getRating();
		Double K = 32.0;
		Double winnerRVal = Math.pow((double)10, (double)(winnerInitialRating / 400));
		Double loserRVal = Math.pow((double)10, (double)(loserInitialRating / 400));
		Double winnerEVal = winnerRVal / (winnerRVal + loserRVal);
		Double loserEVal = loserRVal / (winnerRVal + loserRVal);
		newWinnerRating = (int) Math.floor(winnerInitialRating + K * (1 - winnerEVal));
		newLoserRating = (int) Math.floor(loserInitialRating + K * (0 - loserEVal));
		logger.info(String.format("Rating update: AccountId: %s %d-%d  AccountId %s %d-%d", 
				winnerRating.getAccountId().toString(), winnerInitialRating, newWinnerRating,
				loserRating.getAccountId().toString(), loserInitialRating, newLoserRating));
		return Pair.of(newWinnerRating, newLoserRating);
	}
}
