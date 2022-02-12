package com.netren.testapp.repository.repositories.pagination

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.netren.testapp.repository.repositories.models.Post

typealias PostsPageLoader = suspend (pageSize: Int, pageIndex: Int) -> List<Post>

class PostsPagingSource(
    private val loader: PostsPageLoader,
    private val pageSize: Int
) : PagingSource<Int, Post>() {

    override fun getRefreshKey(state: PagingState<Int, Post>): Int? {
        // get the most recently accessed index in the users list:
        val anchorPosition = state.anchorPosition ?: return null
        // convert item index to page index:
        val page = state.closestPageToPosition(anchorPosition) ?: return null
        // page doesn't have 'currentKey' property, so need to calculate it manually:
        return page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Post> {
        // get the index of page to be loaded (it may be NULL, in this case let's load the first page with index = 0)
        val pageIndex = params.key ?: 0

        return try {

            Log.d("AAA", "val users = loader.invoke($pageIndex, ${params.loadSize})")

            // loading the desired page of users
            val users = loader.invoke(pageIndex, params.loadSize)
            // success! now we can return LoadResult.Page

            val data = users

            Log.d("AAA", "data.size = ${data.size}")


            val prevKey =
                if (pageIndex == 0)
                    null
                else
                    pageIndex - 1

            Log.d("AAA", "prevKey = $prevKey")

            val nextKey =
                if (data.size == params.loadSize)
                    pageIndex + (params.loadSize / pageSize)
                else
                    null

            Log.d("AAA", "nextKey = $nextKey")

            val tmp = LoadResult.Page(
                data = data,
                // index of the previous page if exists
                prevKey = prevKey,
                // index of the next page if exists;
                // please note that 'params.loadSize' may be larger for the first load (by default x3 times)
                nextKey = nextKey
            )

            return tmp
        } catch (e: Exception) {

            // failed to load users -> need to return LoadResult.Error
            LoadResult.Error(
                throwable = e
            )
        }
    }
}