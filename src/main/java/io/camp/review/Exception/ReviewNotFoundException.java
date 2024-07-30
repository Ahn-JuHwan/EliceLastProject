package io.camp.review.Exception;
public class ReviewNotFoundException extends RuntimeException {
    public ReviewNotFoundException(Long id) {
        super("Review not found with id: " + id);
    }
}


