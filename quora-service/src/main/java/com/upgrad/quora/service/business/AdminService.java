package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserAuthDao;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    @Autowired
    private UserAuthDao userAuthDao;

    @Autowired
    private UserDao userDao;

    /**
     * Deletes the user form the database.
     *
     * @param userId      ID of the user to be deleted.
     * @param accessToken To authenticate if the user who is tying to delete the user.
     * @return
     * @throws AuthorizationFailedException ATHR-001 if the access token is invalid or ATHR-002
     *                                      already logged out or ATHR-003 user is not an admin or user with enetered uuid does not
     *                                      exist
     * @throws UserNotFoundException        USR-001 if the user with given id is not present in the records.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity deleteUser(final String userId, final String accessToken)
            throws AuthorizationFailedException, UserNotFoundException {
        UserAuthEntity userAuthEntity = this.userAuthDao.getUserAuthByToken (accessToken);

        if (userAuthEntity == null) {
            throw new AuthorizationFailedException ("ATHR-001", "User has not signed in");
        }

        if (userAuthEntity.getLogoutAt () != null) {
            throw new AuthorizationFailedException ("ATHR-002", "User is signed out");
        }

        if (!userAuthEntity.getUserEntity ().getRole ().equals ("admin")) {
            throw new AuthorizationFailedException (
                    "ATHR-003", "Unauthorized Access, Entered user is not an admin");
        }

        UserEntity existingUser = this.userDao.getUserById (userId);

        if (existingUser == null) {
            throw new UserNotFoundException (
                    "USR-001", "User with entered uuid to be deleted does not exist");
        }

        UserEntity deletedUser = this.userDao.deleteUser (userId);
        return deletedUser;
    }
}